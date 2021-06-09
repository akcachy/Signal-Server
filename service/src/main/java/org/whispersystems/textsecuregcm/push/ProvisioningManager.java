/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.push;

import com.google.protobuf.ByteString;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.storage.PubSubManager;
import org.whispersystems.textsecuregcm.storage.PubSubProtos;
import org.whispersystems.textsecuregcm.websocket.ProvisioningAddress;

import static com.codahale.metrics.MetricRegistry.name;

public class ProvisioningManager {
    private final PubSubManager pubSubManager;

    private final Counter provisioningMessageOnlineCounter  = Metrics.counter(name(getClass(), "sendProvisioningMessage"), "online", "true");
    private final Counter provisioningMessageOfflineCounter = Metrics.counter(name(getClass(), "sendProvisioningMessage"), "online", "false");
    private final Logger logger = LoggerFactory.getLogger(ProvisioningManager.class);
    public ProvisioningManager(final PubSubManager pubSubManager) {
        this.pubSubManager = pubSubManager;
    }

    public boolean sendProvisioningMessage(ProvisioningAddress address, byte[] body) {
        PubSubProtos.PubSubMessage pubSubMessage = PubSubProtos.PubSubMessage.newBuilder()
                .setType(PubSubProtos.PubSubMessage.Type.DELIVER)
                .setContent(ByteString.copyFrom(body))
                .build();

        logger.info("-------------------- SEND PROVISIONING MESSAGE -----------TYPE "+pubSubMessage.getType()+" DEVICE ID " +address.getDeviceId()+ "  NUMBER "+address.getNumber() + "  ADDRESS "+address.getAddress());
        if (pubSubManager.publish(address, pubSubMessage)) {
            logger.info("------------------PUBLISH TRUE ");
            provisioningMessageOnlineCounter.increment();
            return true;
        } else {
            logger.info("------------------PUBLISH FALSE ");
            provisioningMessageOfflineCounter.increment();
            return false;
        }
    }
}
