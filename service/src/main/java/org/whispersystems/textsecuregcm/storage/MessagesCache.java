/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.InvalidProtocolBufferException;
import io.dropwizard.lifecycle.Managed;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.ZAddArgs;
import io.lettuce.core.cluster.SlotHash;
import io.lettuce.core.cluster.event.ClusterTopologyChangedEvent;
import io.lettuce.core.cluster.models.partitions.RedisClusterNode;
import io.lettuce.core.cluster.pubsub.RedisClusterPubSubAdapter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.entities.CachyComment;
import org.whispersystems.textsecuregcm.entities.CachyUserPostResponse;
import org.whispersystems.textsecuregcm.entities.MessageProtos;
import org.whispersystems.textsecuregcm.entities.OutgoingMessageEntity;
import org.whispersystems.textsecuregcm.redis.ClusterLuaScript;
import org.whispersystems.textsecuregcm.redis.FaultTolerantPubSubConnection;
import org.whispersystems.textsecuregcm.redis.FaultTolerantRedisCluster;
import org.whispersystems.textsecuregcm.util.RedisClusterUtil;
import org.whispersystems.textsecuregcm.util.SystemMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static com.codahale.metrics.MetricRegistry.name;

public class MessagesCache extends RedisClusterPubSubAdapter<String, String> implements Managed {

    private final FaultTolerantRedisCluster insertCluster;
    private final FaultTolerantRedisCluster readDeleteCluster;
    private final FaultTolerantPubSubConnection<String, String> pubSubConnection;

    private final ExecutorService notificationExecutorService;

    private final ClusterLuaScript insertScript;
    private final ClusterLuaScript insertMultiPostScript;
    private final ClusterLuaScript removeByIdScript;
    private final ClusterLuaScript removeBySenderScript;
    private final ClusterLuaScript removeByGuidScript;
    private final ClusterLuaScript getItemsScript;
    private final ClusterLuaScript getPostsScript;
    private final ClusterLuaScript removeQueueScript;
    private final ClusterLuaScript getQueuesToPersistScript;
    private final ClusterLuaScript getLikesScript;

    private final Map<String, MessageAvailabilityListener> messageListenersByQueueName = new HashMap<>();
    private final Map<MessageAvailabilityListener, String> queueNamesByMessageListener = new IdentityHashMap<>();

    private final Timer   insertTimer                         = Metrics.timer(name(MessagesCache.class, "insert"), "ephemeral", "false");
    private final Timer   getLikesTimer                         = Metrics.timer(name(MessagesCache.class, "getLikes"), "ephemeral", "false");
    private final Timer   insertEphemeralTimer                = Metrics.timer(name(MessagesCache.class, "insert"), "ephemeral", "true");
    private final Timer   getMessagesTimer                    = Metrics.timer(name(MessagesCache.class, "get"));
    private final Timer   getQueuesToPersistTimer             = Metrics.timer(name(MessagesCache.class, "getQueuesToPersist"));
    private final Timer   clearQueueTimer                     = Metrics.timer(name(MessagesCache.class, "clear"));
    private final Timer   takeEphemeralMessageTimer           = Metrics.timer(name(MessagesCache.class, "takeEphemeral"));
    private final Timer   takePostWallMessageTimer           = Metrics.timer(name(MessagesCache.class, "takePostWall"));
    private final Counter pubSubMessageCounter                = Metrics.counter(name(MessagesCache.class, "pubSubMessage"));
    private final Counter newMessageNotificationCounter       = Metrics.counter(name(MessagesCache.class, "newMessageNotification"), "ephemeral", "false");
    private final Counter ephemeralMessageNotificationCounter = Metrics.counter(name(MessagesCache.class, "newMessageNotification"), "ephemeral", "true");
    private final Counter ephemeralMatchingMessageNotificationCounter = Metrics.counter(name(MessagesCache.class, "newMatchingMessageNotification"));
    private final Counter postWallMessageNotificationCounter = Metrics.counter(name(MessagesCache.class, "postWallMessageNotification"));
    private final Counter queuePersistedNotificationCounter   = Metrics.counter(name(MessagesCache.class, "queuePersisted"));

    static final         String NEXT_SLOT_TO_PERSIST_KEY  = "user_queue_persist_slot";
    private static final byte[] LOCK_VALUE                = "1".getBytes(StandardCharsets.UTF_8);

    private static final String QUEUE_KEYSPACE_PREFIX           = "__keyspace@0__:user_queue::";
    private static final String EPHEMERAL_QUEUE_KEYSPACE_PREFIX = "__keyspace@0__:user_queue_ephemeral::";
    private static final String PERSISTING_KEYSPACE_PREFIX      = "__keyspace@0__:user_queue_persisting::";
    private static final String MATCHER_QUEUE_KEYSPACE_PREFIX           = "__keyspace@0__:user_matcher_queue::";
    private static final String POSTS_QUEUE_KEYSPACE_PREFIX           = "__keyspace@0__:user_posts_queue::";
    private static final String POSTS_WALL_QUEUE_KEYSPACE_PREFIX      = "__keyspace@0__:user_posts_wall_queue::";

    private static final Duration MAX_EPHEMERAL_MESSAGE_DELAY = Duration.ofSeconds(10);

    private static final String REMOVE_TIMER_NAME = name(MessagesCache.class, "remove");

    private static final String REMOVE_METHOD_TAG    = "method";
    private static final String REMOVE_METHOD_ID     = "id";
    private static final String REMOVE_METHOD_SENDER = "sender";
    private static final String REMOVE_METHOD_UUID   = "uuid";

    private static final Logger logger = LoggerFactory.getLogger(MessagesCache.class);
    private final ObjectMapper                         mapper = SystemMapper.getMapper();
    public MessagesCache(final FaultTolerantRedisCluster insertCluster, final FaultTolerantRedisCluster readDeleteCluster, final ExecutorService notificationExecutorService) throws IOException {

        this.insertCluster = insertCluster;
        this.readDeleteCluster = readDeleteCluster;
        this.pubSubConnection = readDeleteCluster.createPubSubConnection();

        this.notificationExecutorService = notificationExecutorService;

        this.insertScript             = ClusterLuaScript.fromResource(insertCluster, "lua/insert_item.lua",           ScriptOutputType.INTEGER);
        this.insertMultiPostScript         = ClusterLuaScript.fromResource(readDeleteCluster, "lua/insert_multiple_post.lua",     ScriptOutputType.VALUE);
        this.removeByIdScript         = ClusterLuaScript.fromResource(readDeleteCluster, "lua/remove_item_by_id.lua",     ScriptOutputType.VALUE);
        this.removeBySenderScript     = ClusterLuaScript.fromResource(readDeleteCluster, "lua/remove_item_by_sender.lua", ScriptOutputType.VALUE);
        this.removeByGuidScript       = ClusterLuaScript.fromResource(readDeleteCluster, "lua/remove_item_by_guid.lua",   ScriptOutputType.MULTI);
        this.getItemsScript           = ClusterLuaScript.fromResource(readDeleteCluster, "lua/get_items.lua",             ScriptOutputType.MULTI);
        this.removeQueueScript        = ClusterLuaScript.fromResource(readDeleteCluster, "lua/remove_queue.lua",          ScriptOutputType.STATUS);
        this.getQueuesToPersistScript = ClusterLuaScript.fromResource(readDeleteCluster, "lua/get_queues_to_persist.lua", ScriptOutputType.MULTI);
        this.getPostsScript               = ClusterLuaScript.fromResource(readDeleteCluster, "lua/get_posts.lua",             ScriptOutputType.MULTI);
        this.getLikesScript             = ClusterLuaScript.fromResource(insertCluster, "lua/get_likes.lua",           ScriptOutputType.INTEGER);
    }

    @Override
    public void start() {
        pubSubConnection.usePubSubConnection(connection -> {
            connection.addListener(this);
            connection.getResources().eventBus().get()
                    .filter(event -> event instanceof ClusterTopologyChangedEvent)
                    .subscribe(event -> resubscribeAll());
        });
    }

    @Override
    public void stop() {
        pubSubConnection.usePubSubConnection(connection -> connection.sync().masters().commands().unsubscribe());
    }

    private void resubscribeAll() {
        logger.info("Got topology change event, resubscribing all keyspace notifications");

        final Set<String> queueNames;

        synchronized (messageListenersByQueueName) {
            queueNames = new HashSet<>(messageListenersByQueueName.keySet());
        }

        for (final String queueName : queueNames) {
            subscribeForKeyspaceNotifications(queueName);
        }
    }

    public long insert(final UUID guid, final UUID destinationUuid, final long destinationDevice, final MessageProtos.Envelope message) {
        final MessageProtos.Envelope messageWithGuid = message.toBuilder().setServerGuid(guid.toString()).build();
        final String                 sender          = message.hasSource() ? (message.getSource() + "::" + message.getTimestamp()) : "nil";
        logger.warn("**************** MESSAGE CACHE INSERT() MSG_UUID "+ guid.toString()+ " DESTINATION UUID "+ destinationUuid.toString());
        return (long)insertTimer.record(() ->
                insertScript.executeBinary(List.of(getMessageQueueKey(destinationUuid, destinationDevice),
                                                   getMessageQueueMetadataKey(destinationUuid, destinationDevice),
                                                   getQueueIndexKey(destinationUuid, destinationDevice)),
                                           List.of(messageWithGuid.toByteArray(),
                                                   String.valueOf(message.getTimestamp()).getBytes(StandardCharsets.UTF_8),
                                                   sender.getBytes(StandardCharsets.UTF_8),
                                                   guid.toString().getBytes(StandardCharsets.UTF_8))));
    }

    public void insertEphemeral(final UUID destinationUuid, final long destinationDevice, final MessageProtos.Envelope message) {
        logger.warn("**************** MESSAGE CACHE INSERTEMPHEMERAL() DESTINATION UUID "+ destinationUuid.toString());
        insertEphemeralTimer.record(() -> {
                final byte[] ephemeralQueueKey = getEphemeralMessageQueueKey(destinationUuid, destinationDevice);

                insertCluster.useBinaryCluster(connection -> {
                    connection.sync().rpush(ephemeralQueueKey, message.toByteArray());
                    connection.sync().expire(ephemeralQueueKey, MAX_EPHEMERAL_MESSAGE_DELAY.toSeconds());
                });
        });
    }

    public Optional<OutgoingMessageEntity> remove(final UUID destinationUuid, final long destinationDevice, final long id) {
        try {
            final byte[] serialized = (byte[])Metrics.timer(REMOVE_TIMER_NAME, REMOVE_METHOD_TAG, REMOVE_METHOD_ID).record(() ->
                    removeByIdScript.executeBinary(List.of(getMessageQueueKey(destinationUuid, destinationDevice),
                                                           getMessageQueueMetadataKey(destinationUuid, destinationDevice),
                                                           getQueueIndexKey(destinationUuid, destinationDevice)),
                                                   List.of(String.valueOf(id).getBytes(StandardCharsets.UTF_8))));

            if (serialized != null) {
                return Optional.of(constructEntityFromEnvelope(id, MessageProtos.Envelope.parseFrom(serialized)));
            }
        } catch (final InvalidProtocolBufferException e) {
            logger.warn("Failed to parse envelope", e);
        }

        return Optional.empty();
    }

    public Optional<OutgoingMessageEntity> remove(final UUID destinationUuid, final long destinationDevice, final String sender, final long timestamp) {
        try {
            final byte[] serialized = (byte[])Metrics.timer(REMOVE_TIMER_NAME, REMOVE_METHOD_TAG, REMOVE_METHOD_SENDER).record(() ->
                    removeBySenderScript.executeBinary(List.of(getMessageQueueKey(destinationUuid, destinationDevice),
                                                               getMessageQueueMetadataKey(destinationUuid, destinationDevice),
                                                               getQueueIndexKey(destinationUuid, destinationDevice)),
                                                       List.of((sender + "::" + timestamp).getBytes(StandardCharsets.UTF_8))));

            if (serialized != null) {
                return Optional.of(constructEntityFromEnvelope(0, MessageProtos.Envelope.parseFrom(serialized)));
            }
        } catch (final InvalidProtocolBufferException e) {
            logger.warn("Failed to parse envelope", e);
        }

        return Optional.empty();
    }

    public Optional<OutgoingMessageEntity> remove(final UUID destinationUuid, final long destinationDevice, final UUID messageGuid) {
        return remove(destinationUuid, destinationDevice, List.of(messageGuid)).stream().findFirst();
    }

    @SuppressWarnings("unchecked")
    public List<OutgoingMessageEntity> remove(final UUID destinationUuid, final long destinationDevice, final List<UUID> messageGuids) {
        final List<byte[]> serialized = (List<byte[]>)Metrics.timer(REMOVE_TIMER_NAME, REMOVE_METHOD_TAG, REMOVE_METHOD_UUID).record(() ->
                removeByGuidScript.executeBinary(List.of(getMessageQueueKey(destinationUuid, destinationDevice),
                                                         getMessageQueueMetadataKey(destinationUuid, destinationDevice),
                                                         getQueueIndexKey(destinationUuid, destinationDevice)),
                                                 messageGuids.stream().map(guid -> guid.toString().getBytes(StandardCharsets.UTF_8)).collect(Collectors.toList())));

        final List<OutgoingMessageEntity> removedMessages = new ArrayList<>(serialized.size());

        for (final byte[] bytes : serialized) {
            try {
                removedMessages.add(constructEntityFromEnvelope(0, MessageProtos.Envelope.parseFrom(bytes)));
            } catch (final InvalidProtocolBufferException e) {
                logger.warn("Failed to parse envelope", e);
            }
        }

        return removedMessages;
    }

    public boolean hasMessages(final UUID destinationUuid, final long destinationDevice) {
        return readDeleteCluster.withBinaryCluster(connection -> connection.sync().zcard(getMessageQueueKey(destinationUuid, destinationDevice)) > 0);
    }
    public List<CachyComment> getComments(final String postId, final long[] range) {
        final List<byte[]> comments = (List<byte[]>) readDeleteCluster.withBinaryCluster(connection -> connection.sync().lrange(getCommentQueueKey(postId), range[0], range[1])   ); 
        List<CachyComment> list = new ArrayList();
        comments.stream().forEach(comment  ->  {
           
            try{
                String cmt =  new String(comment);
                list.add(mapper.readValue(cmt, CachyComment.class));
                System.out.println(cmt);
            }catch(Exception e){}
        });
        return list;
    }

    @SuppressWarnings("unchecked")
    public List<OutgoingMessageEntity> get(final UUID destinationUuid, final long destinationDevice, final int limit) {
        return getMessagesTimer.record(() -> {
            final List<byte[]> queueItems = (List<byte[]>)getItemsScript.executeBinary(List.of(getMessageQueueKey(destinationUuid, destinationDevice),
                                                                                               getPersistInProgressKey(destinationUuid, destinationDevice)),
                                                                                       List.of(String.valueOf(limit).getBytes(StandardCharsets.UTF_8)));

            final List<OutgoingMessageEntity> messageEntities;

            if (queueItems.size() % 2 == 0) {
                messageEntities = new ArrayList<>(queueItems.size() / 2);

                for (int i = 0; i < queueItems.size() - 1; i += 2) {
                    try {
                        final MessageProtos.Envelope message = MessageProtos.Envelope.parseFrom(queueItems.get(i));
                        final long id = Long.parseLong(new String(queueItems.get(i + 1), StandardCharsets.UTF_8));

                        messageEntities.add(constructEntityFromEnvelope(id, message));
                    } catch (InvalidProtocolBufferException e) {
                        logger.warn("Failed to parse envelope", e);
                    }
                }
            } else {
                logger.error("\"Get messages\" operation returned a list with a non-even number of elements.");
                messageEntities = Collections.emptyList();
            }

            return messageEntities;
        });
    }

    @VisibleForTesting
    List<MessageProtos.Envelope> getMessagesToPersist(final UUID accountUuid, final long destinationDevice, final int limit) {
        return getMessagesTimer.record(() -> {
            final List<ScoredValue<byte[]>>    scoredMessages = readDeleteCluster.withBinaryCluster(connection -> connection.sync().zrangeWithScores(getMessageQueueKey(accountUuid, destinationDevice), 0, limit));
            final List<MessageProtos.Envelope> envelopes      = new ArrayList<>(scoredMessages.size());

            for (final ScoredValue<byte[]> scoredMessage : scoredMessages) {
                try {
                    envelopes.add(MessageProtos.Envelope.parseFrom(scoredMessage.getValue()));
                } catch (InvalidProtocolBufferException e) {
                    logger.warn("Failed to parse envelope", e);
                }
            }

            return envelopes;
        });
    }

    public Optional<MessageProtos.Envelope> takeEphemeralMessage(final UUID destinationUuid, final long destinationDevice) {
        return takeEphemeralMessage(destinationUuid, destinationDevice, System.currentTimeMillis());
    }

    @VisibleForTesting
    Optional<MessageProtos.Envelope> takeEphemeralMessage(final UUID destinationUuid, final long destinationDevice, final long currentTimeMillis) {
        final long earliestAllowableTimestamp = currentTimeMillis - MAX_EPHEMERAL_MESSAGE_DELAY.toMillis();

        return takeEphemeralMessageTimer.record(() -> readDeleteCluster.withBinaryCluster(connection -> {
            byte[] messageBytes;

            while ((messageBytes = connection.sync().lpop(getEphemeralMessageQueueKey(destinationUuid, destinationDevice))) != null) {
                try {
                    final MessageProtos.Envelope message = MessageProtos.Envelope.parseFrom(messageBytes);

                    if (message.getTimestamp() >= earliestAllowableTimestamp) {
                        return Optional.of(message);
                    }
                } catch (final InvalidProtocolBufferException e) {
                    logger.warn("Failed to parse envelope", e);
                }
            }

            return Optional.empty();
        }));
    }

    public Optional<String> takeMatchingMessage(final UUID destinationUuid, final long destinationDevice) {
        return takeMatchingMessage(destinationUuid, destinationDevice, System.currentTimeMillis());
    }
    
    public Optional<CachyUserPostResponse> takePostWallMessage(final UUID destinationUuid, final long destinationDevice) {
        return takePostWallMessage(destinationUuid, destinationDevice, System.currentTimeMillis());
    }

    @VisibleForTesting
    Optional<CachyUserPostResponse> takePostWallMessage(final UUID uuid, final long destinationDevice, final long currentTimeMillis) {
        long[] range = new long[2];
        range[0] = 0;
        range[1] = 19;
        List<CachyUserPostResponse> list = getPosts(uuid, destinationDevice, range, true, false, true);
        if(list.size()!=1){
            return Optional.of(list.get(0));
        }
        return Optional.empty();
    }

    @VisibleForTesting
    Optional<String> takeMatchingMessage(final UUID destinationUuid, final long destinationDevice, final long currentTimeMillis) {
        final long earliestAllowableTimestamp = currentTimeMillis - MAX_EPHEMERAL_MESSAGE_DELAY.toMillis();

        return takeEphemeralMessageTimer.record(() -> readDeleteCluster.withBinaryCluster(connection -> {
            byte[] messageBytes;

            while ((messageBytes = connection.sync().lpop(getMatecingMessageQueueKey(destinationUuid, destinationDevice))) != null) {
                //try {
                    String msg = new String(messageBytes);
                    logger.info("########## MATCHING "+ msg);
                    return Optional.of(msg);
                //}
            }

            return Optional.empty();
        }));
    }

    public void clear(final UUID destinationUuid) {
        // TODO Remove null check in a fully UUID-based world
        if (destinationUuid != null) {
            for (int i = 1; i < 256; i++) {
                clear(destinationUuid, i);
            }
        }
    }

    public void clear(final UUID destinationUuid, final long deviceId) {
        clearQueueTimer.record(() ->
                removeQueueScript.executeBinary(List.of(getMessageQueueKey(destinationUuid, deviceId),
                                                        getMessageQueueMetadataKey(destinationUuid, deviceId),
                                                        getQueueIndexKey(destinationUuid, deviceId)),
                                                Collections.emptyList()));
    }

    int getNextSlotToPersist() {
        return (int)(readDeleteCluster.withCluster(connection -> connection.sync().incr(NEXT_SLOT_TO_PERSIST_KEY)) % SlotHash.SLOT_COUNT);
    }

    List<String> getQueuesToPersist(final int slot, final Instant maxTime, final int limit) {
        //noinspection unchecked
        return getQueuesToPersistTimer.record(() -> (List<String>)getQueuesToPersistScript.execute(List.of(new String(getQueueIndexKey(slot), StandardCharsets.UTF_8)),
                                                                                                   List.of(String.valueOf(maxTime.toEpochMilli()),
                                                                                                           String.valueOf(limit))));
    }

    void addQueueToPersist(final UUID accountUuid, final long deviceId) {
        readDeleteCluster.useBinaryCluster(connection -> connection.sync().zadd(getQueueIndexKey(accountUuid, deviceId), ZAddArgs.Builder.nx(), System.currentTimeMillis(), getMessageQueueKey(accountUuid, deviceId)));
    }

    void lockQueueForPersistence(final UUID accountUuid, final long deviceId) {
        readDeleteCluster.useBinaryCluster(connection -> connection.sync().setex(getPersistInProgressKey(accountUuid, deviceId), 30, LOCK_VALUE));
    }

    void unlockQueueForPersistence(final UUID accountUuid, final long deviceId) {
        readDeleteCluster.useBinaryCluster(connection -> connection.sync().del(getPersistInProgressKey(accountUuid, deviceId)));
    }

    public void addMessageAvailabilityListener(final UUID destinationUuid, final long deviceId, final MessageAvailabilityListener listener) {
        final String queueName = getQueueName(destinationUuid, deviceId);
        logger.info("MESSAGE AVAILABLITY LISTNER QUEUE NAME "+ queueName);
        synchronized (messageListenersByQueueName) {
            messageListenersByQueueName.put(queueName, listener);
            queueNamesByMessageListener.put(listener, queueName);
        }

        subscribeForKeyspaceNotifications(queueName);
    }

    public void removeMessageAvailabilityListener(final MessageAvailabilityListener listener) {
        final String queueName = queueNamesByMessageListener.remove(listener);

        unsubscribeFromKeyspaceNotifications(queueName);

        synchronized (messageListenersByQueueName) {
            if (queueName != null) {
                messageListenersByQueueName.remove(queueName);
            }
        }
    }

    private void subscribeForKeyspaceNotifications(final String queueName) {
        logger.info("####################### QUEUE NAME " + queueName);
        final int slot = SlotHash.getSlot(queueName);

        pubSubConnection.usePubSubConnection(connection -> connection.sync().nodes(node -> node.is(RedisClusterNode.NodeFlag.MASTER) && node.hasSlot(slot))
                                                                     .commands()
                                                                     .subscribe(getKeyspaceChannels(queueName)));
    }

    private void unsubscribeFromKeyspaceNotifications(final String queueName) {
        pubSubConnection.usePubSubConnection(connection -> connection.sync().masters()
                                                                     .commands()
                                                                     .unsubscribe(getKeyspaceChannels(queueName)));
    }

    private static String[] getKeyspaceChannels(final String queueName) {
        return new String[] {
                QUEUE_KEYSPACE_PREFIX + "{" + queueName + "}",
                EPHEMERAL_QUEUE_KEYSPACE_PREFIX + "{" + queueName + "}",
                PERSISTING_KEYSPACE_PREFIX + "{" + queueName + "}",
                MATCHER_QUEUE_KEYSPACE_PREFIX + "{" + queueName + "}",
                POSTS_WALL_QUEUE_KEYSPACE_PREFIX + "{" + queueName + "}",
        };
    }

    @Override
    public void message(final RedisClusterNode node, final String channel, final String message) {
        pubSubMessageCounter.increment();

        logger.info("####################### MESSAGE  CHANNEL " + channel + " MSG "+message);
        if (channel.startsWith(QUEUE_KEYSPACE_PREFIX) && "zadd".equals(message)) {
            logger.info("####################### CHANNEL START WITH ######## ZADD");
            newMessageNotificationCounter.increment();
            notificationExecutorService.execute(() -> findListener(channel).ifPresent(MessageAvailabilityListener::handleNewMessagesAvailable));
        } else if (channel.startsWith(EPHEMERAL_QUEUE_KEYSPACE_PREFIX) && "rpush".equals(message)) {
            logger.info("####################### CHANNEL START WITH ######## RPUSH");
            ephemeralMessageNotificationCounter.increment();
            notificationExecutorService.execute(() -> findListener(channel).ifPresent(MessageAvailabilityListener::handleNewEphemeralMessageAvailable));
        } else if (channel.startsWith(PERSISTING_KEYSPACE_PREFIX) && "del".equals(message)) {
            logger.info("####################### CHANNEL START WITH ######## DEL");
            queuePersistedNotificationCounter.increment();
            notificationExecutorService.execute(() -> findListener(channel).ifPresent(MessageAvailabilityListener::handleMessagesPersisted));
        }
        else if (channel.startsWith(MATCHER_QUEUE_KEYSPACE_PREFIX) && "rpush".equals(message)) {
            logger.info("####################### CHANNEL START WITH ######## RPUSH MATCHING");
            ephemeralMatchingMessageNotificationCounter.increment();
            notificationExecutorService.execute(() -> findListener(channel).ifPresent(MessageAvailabilityListener::handleNewMatchingMessageAvailable));
        }
        else if (channel.startsWith(POSTS_WALL_QUEUE_KEYSPACE_PREFIX) && "zadd".equals(message)) {
            logger.info("####################### CHANNEL START WITH ######## HSET MATCHING");
            postWallMessageNotificationCounter.increment();
            notificationExecutorService.execute(() -> findListener(channel).ifPresent(MessageAvailabilityListener::handlePostWallMessageAvailable));
        }
    }

    private Optional<MessageAvailabilityListener> findListener(final String keyspaceChannel) {
        final String queueName = getQueueNameFromKeyspaceChannel(keyspaceChannel);

        synchronized (messageListenersByQueueName) {
            return Optional.ofNullable(messageListenersByQueueName.get(queueName));
        }
    }

    @VisibleForTesting
    static OutgoingMessageEntity constructEntityFromEnvelope(long id, MessageProtos.Envelope envelope) {
        return new OutgoingMessageEntity(id, true,
                envelope.hasServerGuid() ? UUID.fromString(envelope.getServerGuid()) : null,
                envelope.getType().getNumber(),
                envelope.getRelay(),
                envelope.getTimestamp(),
                envelope.getSource(),
                envelope.hasSourceUuid() ? UUID.fromString(envelope.getSourceUuid()) : null,
                envelope.getSourceDevice(),
                envelope.hasLegacyMessage() ? envelope.getLegacyMessage().toByteArray() : null,
                envelope.hasContent() ? envelope.getContent().toByteArray() : null,
                envelope.hasServerTimestamp() ? envelope.getServerTimestamp() : 0);
    }

    @VisibleForTesting
    static String getQueueName(final UUID accountUuid, final long deviceId) {
        return accountUuid + "::" + deviceId;
    }

    @VisibleForTesting
    static String getQueueNameFromKeyspaceChannel(final String channel) {
        final int startOfHashTag = channel.indexOf('{');
        final int endOfHashTag = channel.lastIndexOf('}');

        return channel.substring(startOfHashTag + 1, endOfHashTag);
    }

    @VisibleForTesting
    static byte[] getMessageQueueKey(final UUID accountUuid, final long deviceId) {
        return ("user_queue::{" + accountUuid.toString() + "::" + deviceId + "}").getBytes(StandardCharsets.UTF_8);
    }

    static byte[] getEphemeralMessageQueueKey(final UUID accountUuid, final long deviceId) {
        return ("user_queue_ephemeral::{" + accountUuid.toString() + "::" + deviceId + "}").getBytes(StandardCharsets.UTF_8);
    }

    static byte[] getMatecingMessageQueueKey(final UUID accountUuid, final long deviceId) {
        return ("user_matcher_queue::{" + accountUuid.toString() + "::" + deviceId + "}").getBytes(StandardCharsets.UTF_8);
    }

    static byte[] getPostWallMessageQueueKey(final UUID accountUuid, final long deviceId) {
        return ("user_posts_wall_queue::{" + accountUuid.toString() + "::" + deviceId + "}").getBytes(StandardCharsets.UTF_8);
    }
    
    static byte[] getPostsMessageQueueKey(final UUID accountUuid, final long deviceId) {
        return ("user_posts_queue::{" + accountUuid.toString() + "::" + deviceId + "}").getBytes(StandardCharsets.UTF_8);
    }
    private static byte[] getMessageQueueMetadataKey(final UUID accountUuid, final long deviceId) {
        return ("user_queue_metadata::{" + accountUuid.toString() + "::" + deviceId + "}").getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] getQueueIndexKey(final UUID accountUuid, final long deviceId) {
        return getQueueIndexKey(SlotHash.getSlot(accountUuid.toString() + "::" + deviceId));
    }

    private static byte[] getQueueIndexKey(final int slot) {
        return ("user_queue_index::{" + RedisClusterUtil.getMinimalHashTag(slot) + "}").getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] getPersistInProgressKey(final UUID accountUuid, final long deviceId) {
        return ("user_queue_persisting::{" + accountUuid + "::" + deviceId  + "}").getBytes(StandardCharsets.UTF_8);
    }

    static UUID getAccountUuidFromQueueName(final String queueName) {
        final int startOfHashTag = queueName.indexOf('{');

        return UUID.fromString(queueName.substring(startOfHashTag + 1, queueName.indexOf("::", startOfHashTag)));
    }

    static long getDeviceIdFromQueueName(final String queueName) {
        return Long.parseLong(queueName.substring(queueName.lastIndexOf("::") + 2, queueName.lastIndexOf('}')));
    }

    //#region
    @SuppressWarnings("unchecked")
    public List<CachyUserPostResponse> getPosts(final UUID destinationUuid, final long destinationDevice, final long[] range, boolean isPosts, boolean isStory, boolean isWall) {
         return getMessagesTimer.record(() -> {
            final byte[] queueName;
            if(isPosts && !isWall){
                queueName = getPostMessageQueueKey(destinationUuid, destinationDevice);
            }else if(isPosts && isWall){
                queueName = getPostWallQueueKey(destinationUuid, destinationDevice);
            }else if(isStory && !isWall){
                queueName = getStoryMessageQueueKey(destinationUuid, destinationDevice);
            }else{
                queueName = getStoryWallQueueKey(destinationUuid, destinationDevice);
            }

            final List<byte[]> queueItems = (List<byte[]>)getPostsScript.executeBinary(List.of(queueName),
                                                                                       List.of(String.valueOf(range[0]).getBytes(StandardCharsets.UTF_8), String.valueOf(range[1]).getBytes(StandardCharsets.UTF_8)  ));
            
            
            final List<CachyUserPostResponse> messageEntities;
            if (queueItems.size() % 2 == 0) {
                messageEntities = new ArrayList<>(queueItems.size() / 2);

                for (int i = 0; i < queueItems.size() - 1; i += 2) {
                    try {
                        //System.out.println(new String(queueItems.get(i)));
                        CachyUserPostResponse post = mapper.readValue(new String(queueItems.get(i)), CachyUserPostResponse.class);
                        post.setSeq(Long.parseLong(new String(queueItems.get(i + 1), StandardCharsets.UTF_8)));
                        final byte[] likeCount = (byte[])readDeleteCluster.withBinaryCluster(connection -> connection.sync().hget(getLikeQueueKey(post.getPostId()), "count".getBytes()));
                        if(likeCount != null){
                            post.setLikes(Long.parseLong(new String(likeCount)));
                        }

                        final Boolean isLiked = (Boolean)readDeleteCluster.withBinaryCluster(connection -> connection.sync().hexists(getLikeQueueKey(post.getPostId()), destinationUuid.toString().getBytes()));
                        post.setLiked(isLiked);   


                        final byte[] commentCount = (byte[])readDeleteCluster.withBinaryCluster(connection -> connection.sync().hget(getCommentCountQueueKey(post.getPostId()), "count".getBytes()));
                        if(commentCount != null){
                            post.setComments(Long.parseLong(new String(commentCount)));
                        }
                        messageEntities.add(post );
                    } catch (Exception e) {
                        logger.warn("Failed to parse envelope", e);
                    }
                }
            } else {
                logger.error("\"Get messages\" operation returned a list with a non-even number of elements.");
                messageEntities = Collections.emptyList();
            }
            
            return messageEntities;
        });
    }

    public void insertRating(final UUID uuid,  final Integer count, final Double average  ) {
        insertEphemeralTimer.record(() -> {
                final byte[] ephemeralQueueKey = getMessageQueueMetadataKey(uuid, 1);

                insertCluster.useBinaryCluster(connection -> {
                    connection.sync().hset(ephemeralQueueKey, "callcount".getBytes() ,  String.valueOf(count).getBytes());
                    connection.sync().hset(ephemeralQueueKey, "callavgrating".getBytes() , String.valueOf(average).getBytes());
                });
        });
    }
    
    public void insertLikeCount(final UUID uuid,  final Integer count ) {
        insertEphemeralTimer.record(() -> {
                final byte[] ephemeralQueueKey = getMessageQueueMetadataKey(uuid, 1);

                insertCluster.useBinaryCluster(connection -> {
                    connection.sync().hset(ephemeralQueueKey, "likecount".getBytes() ,  String.valueOf(count).getBytes());
                });
        });
    }

    public void insertMultiplePost(final UUID uuid, final List<CachyUserPostResponse> messages) {
        insertTimer.record(() ->
                insertMultiPostScript.executeBinary(List.of(getPostMessageQueueKey(uuid, 1),
                                            getMessageQueueMetadataKey(uuid, 1)),
                                            messages.stream().map(message -> {
                                                    byte [] msg;
                                                    try{
                                                        msg =  mapper.writeValueAsString(message).getBytes(StandardCharsets.UTF_8);
                                                    }catch(Exception e){
                                                        msg = new byte[0];
                                                    }
                                                    return msg;
                                                }
                                            ).collect(Collectors.toList()))  
                
        );     
    }
    public void insertMultipleStory(final UUID uuid, final List<CachyUserPostResponse> messages) {
        insertTimer.record(() ->
                insertMultiPostScript.executeBinary(List.of(getStoryMessageQueueKey(uuid, 1),
                                            getMessageQueueMetadataKey(uuid, 1)),
                                            messages.stream().map(message -> {
                                                    byte [] msg;
                                                    try{
                                                        msg =  mapper.writeValueAsString(message).getBytes(StandardCharsets.UTF_8);
                                                    }catch(Exception e){
                                                        msg = new byte[0];
                                                    }
                                                    return msg;
                                                }
                                            ).collect(Collectors.toList()))  
                
        );     
    }

    @VisibleForTesting
    static byte[] getPostMessageQueueKey(final UUID accountUuid, final long deviceId) {
        return ("user_posts_queue::{" + accountUuid.toString() + "::" + deviceId + "}").getBytes(StandardCharsets.UTF_8);
    }
    
    @VisibleForTesting
    static byte[] getPostWallQueueKey(final UUID accountUuid, final long deviceId) {
        return ("user_posts_wall_queue::{" + accountUuid.toString() + "::" + deviceId + "}").getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] getLikeQueueKey(final String postId) {
        return ("user_posts_like_queue::{" + postId + "}").getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] getCommentCountQueueKey(final String postId) {
        return ("user_posts_comment_count_queue::{" + postId + "}").getBytes(StandardCharsets.UTF_8);
    }
    
    private static byte[] getCommentQueueKey(final String postId) {
        return ("user_posts_comment_queue::{" + postId + "}").getBytes(StandardCharsets.UTF_8);
    }

    static byte[] getStoryMessageQueueKey(final UUID accountUuid, final long deviceId) {
        return ("user_stories_queue::{" + accountUuid.toString() + "::" + deviceId + "}").getBytes(StandardCharsets.UTF_8);
    }

    static byte[] getStoryWallQueueKey(final UUID accountUuid, final long deviceId) {
        return ("user_story_wall_queue::{" + accountUuid.toString() + "::" + deviceId + "}").getBytes(StandardCharsets.UTF_8);
    }
    //#endregion
}
