/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.storage;

import java.util.Map;
import java.util.UUID;
/**
 * A message availability listener is notified when new messages are available for a specific device for a specific
 * account. Availability listeners are also notified when messages are moved from the message cache to long-term storage
 * as an optimization hint to implementing classes.
 */
public interface MessageAvailabilityListener {

    void handleNewMessagesAvailable();

    void handleNewEphemeralMessageAvailable();

    void handleMessagesPersisted();

    void handleNewMatchingMessageAvailable();
    
    void handlePostWallMessageAvailable();

    void professionalStatusAvailable(UUID uuid, Map<String , String> map);

    void recordingConsentMessageAvailable();

    void userDisableMessageAvailable();

    void handleMessageRefund(UUID senderUuid, UUID receiverUuid, long messageId);

    void handleMatchingUserWaitingExpire(UUID uuid);

    void handleRecentlyMatchedUserExpire(UUID firstUuid, UUID secondUuid);

    void handleMonetizeMessageRead(UUID senderUuid, UUID receiverUuid, long messageId);
}
