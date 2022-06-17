/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.push;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.gcm.server.Message;
import org.whispersystems.gcm.server.Result;
import org.whispersystems.gcm.server.Sender;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.storage.AccountsManager;
import org.whispersystems.textsecuregcm.storage.Device;
import org.whispersystems.textsecuregcm.util.CircuitBreakerUtil;
import org.whispersystems.textsecuregcm.util.Constants;
import org.whispersystems.textsecuregcm.util.SystemMapper;
import org.whispersystems.textsecuregcm.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

public class GCMSender {

  private final Logger logger = LoggerFactory.getLogger(GCMSender.class);

  private final MetricRegistry metricRegistry = SharedMetricRegistries.getOrCreate(Constants.METRICS_NAME);
  private final Meter          success        = metricRegistry.meter(name(getClass(), "sent", "success"));
  private final Meter          failure        = metricRegistry.meter(name(getClass(), "sent", "failure"));
  private final Meter          unregistered   = metricRegistry.meter(name(getClass(), "sent", "unregistered"));
  private final Meter          canonical      = metricRegistry.meter(name(getClass(), "sent", "canonical"));

  private final Map<String, Meter> outboundMeters = new HashMap<>() {{
    put("receipt", metricRegistry.meter(name(getClass(), "outbound", "receipt")));
    put("notification", metricRegistry.meter(name(getClass(), "outbound", "notification")));
    put("challenge", metricRegistry.meter(name(getClass(), "outbound", "challenge")));
    put("like", metricRegistry.meter(name(getClass(), "outbound", "like")));
    put("follow", metricRegistry.meter(name(getClass(), "outbound", "follow")));
    put("comment", metricRegistry.meter(name(getClass(), "outbound", "comment")));
  }};

  private final AccountsManager   accountsManager;
  private final Sender            signalSender;
  private final ExecutorService   executor;

  public GCMSender(ExecutorService executor, AccountsManager accountsManager, String signalKey) {
    this(executor, accountsManager, new Sender(signalKey, SystemMapper.getMapper(), 6));

    CircuitBreakerUtil.registerMetrics(metricRegistry, signalSender.getRetry(), Sender.class);
  }

  @VisibleForTesting
  public GCMSender(ExecutorService executor, AccountsManager accountsManager, Sender sender) {
    this.accountsManager = accountsManager;
    this.signalSender    = sender;
    this.executor        = executor;
  }

  public void sendMessage(GcmMessage message) {
    Message.Builder builder = Message.newBuilder()
                                     .withDestination(message.getGcmId())
                                     .withPriority("high");

    String key;

    switch (message.getType()) {
      case NOTIFICATION: key = "notification"; break;
      case CHALLENGE:    key = "challenge";    break;
      case FOLLOW:       key = "follow";       break;
      case FOLLOWING_ACCEPTED:       key = "following_accepted";       break;
      case POST_LIKED:   key = "post_liked";   break;
      case COMMENTED:    key = "commented";    break;
      case CONTRIBUTOR_IN_POST:       key = "contributor_in_post";      break;
      case TAGGED_IN_POST:            key = "tagged_in_post";           break;
      case FOLLOW_REQUEST:            key = "follow_request";           break;
      case PROFESSIONAL_CALL_MISSED:            key = "professional_call_missed";           break;
      case KEYWORD_APPROVED:            key = "keyword_approved";           break;
      case KEYWORD_REJECTED:            key = "keyword_rejected";           break;
      case KEYWORD_UPFOR_VOTE:            key = "keyword_upfor_vote";           break;
      case PREMIUM_REJECTED:            key = "premium_rejected";           break;
      case COMMENTED_REPLY:            key = "commented_reply";           break;
      case PREMIUM_APPROVED:            key = "premium_approved";           break;
      case PROFESSIONAL_APPROVED:            key = "professional_approved";           break;
      case PROFESSIONAL_REJECTED:            key = "professional_rejected";           break;

      default:           throw new AssertionError();
    }

    logger.info("************ SEND GCM  MESSAGE ********** TYPE "+ key );
    Message request = builder.withDataPart(key, message.getData().orElse("")).build();

    CompletableFuture<Result> future = signalSender.send(request);
    markOutboundMeter(key);

    future.handle((result, throwable) -> {
      if (result != null && message.getType() != GcmMessage.Type.CHALLENGE) {
        if (result.isUnregistered() || result.isInvalidRegistrationId()) {
          executor.submit(() -> handleBadRegistration(message));
        } else if (result.hasCanonicalRegistrationId()) {
          executor.submit(() -> handleCanonicalRegistrationId(message, result));
        } else if (!result.isSuccess()) {
          executor.submit(() -> handleGenericError(message, result));
        } else {
          success.mark();
        }
      } else {
        logger.warn("FCM Failed: " + throwable + ", " + throwable.getCause());
      }

      return null;
    });
  }

  private void handleBadRegistration(GcmMessage message) {
    Optional<Account> account = getAccountForEvent(message);

    if (account.isPresent()) {
      //noinspection OptionalGetWithoutIsPresent
      Device device = account.get().getDevice(message.getDeviceId()).get();

      if (device.getUninstalledFeedbackTimestamp() == 0) {
        device.setUninstalledFeedbackTimestamp(Util.todayInMillis());
        accountsManager.update(account.get());
      }
    }

    unregistered.mark();
  }

  private void handleCanonicalRegistrationId(GcmMessage message, Result result) {
    logger.warn(String.format("Actually received 'CanonicalRegistrationId' ::: (canonical=%s), (original=%s)",
                              result.getCanonicalRegistrationId(), message.getGcmId()));

    Optional<Account> account = getAccountForEvent(message);

    if (account.isPresent()) {
      //noinspection OptionalGetWithoutIsPresent
      Device device = account.get().getDevice(message.getDeviceId()).get();
      device.setGcmId(result.getCanonicalRegistrationId());

      accountsManager.update(account.get());
    }

    canonical.mark();
  }

  private void handleGenericError(GcmMessage message, Result result) {
    logger.warn(String.format("Unrecoverable Error ::: (error=%s), (gcm_id=%s), " +
                              "(destination=%s), (device_id=%d)",
                              result.getError(), message.getGcmId(), message.getNumber(),
                              message.getDeviceId()));
    failure.mark();
  }

  private Optional<Account> getAccountForEvent(GcmMessage message) {
    Optional<Account> account = accountsManager.get(message.getNumber());

    if (account.isPresent()) {
      Optional<Device> device = account.get().getDevice(message.getDeviceId());

      if (device.isPresent()) {
        if (message.getGcmId().equals(device.get().getGcmId())) {

          if (device.get().getPushTimestamp() == 0 || System.currentTimeMillis() > (device.get().getPushTimestamp() + TimeUnit.SECONDS.toMillis(10))) {
            return account;
          }
        }
      }
    }

    return Optional.empty();
  }

  private void markOutboundMeter(String key) {
    Meter meter = outboundMeters.get(key);

    if (meter != null) meter.mark();
    else               logger.warn("Unknown outbound key: " + key);
  }
}
