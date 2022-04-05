/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package org.whispersystems.textsecuregcm.storage;

import static com.codahale.metrics.MetricRegistry.name;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.whispersystems.textsecuregcm.entities.MessageProtos.Envelope;
import org.whispersystems.textsecuregcm.entities.CachyTaggedUserProfile;
import org.whispersystems.textsecuregcm.entities.CachyUserPostResponse;
import org.whispersystems.textsecuregcm.entities.OutgoingMessageEntity;
import org.whispersystems.textsecuregcm.entities.OutgoingMessageEntityList;
import org.whispersystems.textsecuregcm.metrics.PushLatencyManager;
import org.whispersystems.textsecuregcm.redis.RedisOperation;
import org.whispersystems.textsecuregcm.util.Constants;

public class MessagesManager {

  private static final int RESULT_SET_CHUNK_SIZE = 100;

  private static final MetricRegistry metricRegistry       = SharedMetricRegistries.getOrCreate(Constants.METRICS_NAME);
  private static final Meter          cacheHitByNameMeter  = metricRegistry.meter(name(MessagesManager.class, "cacheHitByName" ));
  private static final Meter          cacheMissByNameMeter = metricRegistry.meter(name(MessagesManager.class, "cacheMissByName"));
  private static final Meter          cacheHitByGuidMeter  = metricRegistry.meter(name(MessagesManager.class, "cacheHitByGuid" ));
  private static final Meter          cacheMissByGuidMeter = metricRegistry.meter(name(MessagesManager.class, "cacheMissByGuid"));

  private final MessagesDynamoDb messagesDynamoDb;
  private final MessagesCache messagesCache;
  private final PushLatencyManager pushLatencyManager;

  public MessagesManager(
      MessagesDynamoDb messagesDynamoDb,
      MessagesCache messagesCache,
      PushLatencyManager pushLatencyManager) {
    this.messagesDynamoDb = messagesDynamoDb;
    this.messagesCache = messagesCache;
    this.pushLatencyManager = pushLatencyManager;
  }

  public void insert(UUID destinationUuid, long destinationDevice, Envelope message) {
    messagesCache.insert(UUID.randomUUID(), destinationUuid, destinationDevice, message);
  }

  public void insertEphemeral(final UUID destinationUuid, final long destinationDevice, final Envelope message) {
    messagesCache.insertEphemeral(destinationUuid, destinationDevice, message);
  }

  public void markReadMonetizeMsg(final UUID uuid, final UUID destinationUuid) {
     messagesCache.markReadMonetizeMsg(uuid, destinationUuid);
  }

  public Optional<Envelope> takeEphemeralMessage(final UUID destinationUuid, final long destinationDevice) {
    return messagesCache.takeEphemeralMessage(destinationUuid, destinationDevice);
  }

  public Optional<String> takeMatchingMessage(final UUID destinationUuid, final long destinationDevice) {
    return messagesCache.takeMatchingMessage(destinationUuid, destinationDevice);
  }
  
  public Optional<CachyUserPostResponse> takePostWallMessage(final UUID destinationUuid, final long destinationDevice) {
    return messagesCache.takePostWallMessage(destinationUuid, destinationDevice);
  }
  
  public Optional<String> takeRecordingConsentMessage(final UUID destinationUuid, final long destinationDevice) {
    return messagesCache.takeRecordingConsentMessage(destinationUuid, destinationDevice);
  }
  
  public Map<String , String> takeProfessionalStatusMessage(UUID uuid) {
    return messagesCache.takeProfessionalStatusMessage(uuid);
  }
  public void broadCastMessage(UUID uuid, Map<String , String> msg) {
    messagesCache.broadCastMessage(uuid, msg);
  }
  public void sendEmailMessage(UUID uuid, String email) {
    messagesCache.sendEmailMessage(uuid, email);
  }
  public boolean isUserOnline(UUID uuid) {
    return messagesCache.isUserOnline(uuid);
  }
  public void changeProfessionalQueue(UUID uuid, List<String> categoriesAndAgeGroup, String oldStatus, String newStatus) {
    messagesCache.changeProfessionalQueue(uuid, categoriesAndAgeGroup, oldStatus, newStatus);
  }

  public boolean hasCachedMessages(final UUID destinationUuid, final long destinationDevice) {
    return messagesCache.hasMessages(destinationUuid, destinationDevice);
  }

  public List<CachyUserPostResponse> getPosts(final UUID uuid, final long device, final long[] range) {
    return messagesCache.getPosts(uuid, device, range, true, false, false, null, false, 0);
  }
  public List<CachyUserPostResponse> getPostWall(final UUID uuid, final long device, final long[] range) {
    return messagesCache.getPosts(uuid, device, range, true, false, true, null, false, 0);
  }
  
  public List<CachyUserPostResponse> getStory(final UUID uuid, final long device, final long[] range, final int STORY_EXPIRE_TIME_IN_HOURS) {
    return messagesCache.getPosts(uuid, device, range, false, true, false, null, false, STORY_EXPIRE_TIME_IN_HOURS);
  }
  public List<CachyUserPostResponse> getStoryWall(final UUID uuid, final long device, final long[] range, final int STORY_EXPIRE_TIME_IN_HOURS) {
    return messagesCache.getPosts(uuid, device, range, false, true, true, null, false, STORY_EXPIRE_TIME_IN_HOURS);
  }

  public List<CachyUserPostResponse> getPostByCategory(final UUID uuid, final long device, final long[] range, String categoryAndAgeGroup) {
    return messagesCache.getPosts(uuid, device, range, false, false, false, categoryAndAgeGroup, true, 0);
  }
  
  public Set<String> getDiscoveryPostId(final UUID uuid, final long device, final long[] range, String categoryAndAgeGroup) {
    return messagesCache.getDiscoveryPostId(uuid, device, range, categoryAndAgeGroup);
  }

  public List<CachyUserPostResponse> getPostData(final UUID uuid, Set<String> postList) {
    return messagesCache.getPostData(uuid, postList);
  }
  public void setRecordingConsent(final UUID uuid,  String callId) {
    messagesCache.setRecordingConsent(uuid, callId);
  }

  public void addScheduleTimeKey(final UUID uuid, final long device, final long startttl, final long endttl, int slotIndex) {
     messagesCache.addScheduleTimeKey(uuid, device, startttl, endttl, slotIndex);
  }
  public void unsubscribeFromKeyspaceNotificationsAndRemoveSchedule(final String queueName, final int slotIndex) {
    messagesCache.unsubscribeFromKeyspaceNotificationsAndRemoveSchedule(queueName, slotIndex);
  }
  public void setOnlineStatusOnDisconnect(final UUID uuid) {
     messagesCache.setOnlineStatusOnDisconnect(uuid);
  }
  public void subscribeForKeyspaceNotificationsForProfessionalUsers(final String uuid, int slotIndex) {
     messagesCache.subscribeForKeyspaceNotificationsForProfessionalUsers(uuid, slotIndex);
  }

  public  Map<String , Double> getCommonInterestedCategory(String language, String countryCode, int ageGroup) {
    return messagesCache.getCommonInterestedCategory(language, countryCode, ageGroup);
  }
  public List<CachyTaggedUserProfile> getContributorsStory(final List<CachyTaggedUserProfile> contributorsDetails) {
    return messagesCache.getContributorsStory(contributorsDetails);
  }
  public void insertMultiplePost(final UUID uuid, final List<CachyUserPostResponse> list) {
     messagesCache.insertMultiplePost(uuid,  list);
  }
  public void insertMultipleStory(final UUID uuid, final List<CachyUserPostResponse> list) {
     messagesCache.insertMultipleStory(uuid,  list);
  }
  public void addUserInterest(final UUID uuid, final Map<String , Double> data) {
     messagesCache.addUserInterest(uuid,  data);
  }
  public Map<String, Double> getUserInterest(final UUID uuid) {
     return messagesCache.getUserInterest(uuid);
  }
  public OutgoingMessageEntityList getMessagesForDevice(UUID destinationUuid, long destinationDevice, final String userAgent, final boolean cachedMessagesOnly) {
    RedisOperation.unchecked(() -> pushLatencyManager.recordQueueRead(destinationUuid, destinationDevice, userAgent));

    List<OutgoingMessageEntity> messageList = new ArrayList<>();

    if (!cachedMessagesOnly) {
      messageList.addAll(messagesDynamoDb.load(destinationUuid, destinationDevice, RESULT_SET_CHUNK_SIZE));
    }

    if (messageList.size() < RESULT_SET_CHUNK_SIZE) {
      messageList.addAll(messagesCache.get(destinationUuid, destinationDevice, RESULT_SET_CHUNK_SIZE - messageList.size()));
    }

    return new OutgoingMessageEntityList(messageList, messageList.size() >= RESULT_SET_CHUNK_SIZE);
  }

  public void clear(UUID destinationUuid) {
    messagesCache.clear(destinationUuid);
    messagesDynamoDb.deleteAllMessagesForAccount(destinationUuid);
  }

  public void clear(UUID destinationUuid, long deviceId) {
    messagesCache.clear(destinationUuid, deviceId);
    messagesDynamoDb.deleteAllMessagesForDevice(destinationUuid, deviceId);
  }

  public Optional<OutgoingMessageEntity> delete(
      UUID destinationUuid, long destinationDeviceId, String source, long timestamp) {
    Optional<OutgoingMessageEntity> removed = messagesCache.remove(destinationUuid, destinationDeviceId, source, timestamp);

    if (removed.isEmpty()) {
      removed = messagesDynamoDb.deleteMessageByDestinationAndSourceAndTimestamp(destinationUuid, destinationDeviceId, source, timestamp);
      cacheMissByNameMeter.mark();
    } else {
      cacheHitByNameMeter.mark();
    }

    return removed;
  }

  public Optional<OutgoingMessageEntity> delete(UUID destinationUuid, long destinationDeviceId, UUID guid) {
    Optional<OutgoingMessageEntity> removed = messagesCache.remove(destinationUuid, destinationDeviceId, guid);

    if (removed.isEmpty()) {
      removed = messagesDynamoDb.deleteMessageByDestinationAndGuid(destinationUuid, destinationDeviceId, guid);
      cacheMissByGuidMeter.mark();
    } else {
      cacheHitByGuidMeter.mark();
    }

    return removed;
  }

  public void persistMessages(
      final UUID destinationUuid,
      final long destinationDeviceId,
      final List<Envelope> messages) {
    messagesDynamoDb.store(messages, destinationUuid, destinationDeviceId);
    messagesCache.remove(destinationUuid, destinationDeviceId, messages.stream().map(message -> UUID.fromString(message.getServerGuid())).collect(Collectors.toList()));
  }

  public void addMessageAvailabilityListener(
      final UUID destinationUuid,
      final long destinationDeviceId,
      final MessageAvailabilityListener listener) {
    messagesCache.addMessageAvailabilityListener(destinationUuid, destinationDeviceId, listener);
  }

  public void addTransactionMessageAvailabilityListener(
      final MessageAvailabilityListener listener) {
    messagesCache.addTransactionMessageAvailabilityListener(listener);
  }

  public void removeMessageAvailabilityListener(final MessageAvailabilityListener listener) {
    messagesCache.removeMessageAvailabilityListener(listener);
  }
}
