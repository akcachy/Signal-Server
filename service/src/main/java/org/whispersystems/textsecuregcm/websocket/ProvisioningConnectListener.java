/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.storage.PubSubManager;
import org.whispersystems.websocket.session.WebSocketSessionContext;
import org.whispersystems.websocket.setup.WebSocketConnectListener;

public class ProvisioningConnectListener implements WebSocketConnectListener {

  private final PubSubManager pubSubManager;

  private final Logger logger = LoggerFactory.getLogger(ProvisioningConnectListener.class);
  public ProvisioningConnectListener(PubSubManager pubSubManager) {
    logger.info("################ WEBSOCKET ProvisioningConnectListener ");
    this.pubSubManager = pubSubManager;
  }

  @Override
  public void onWebSocketConnect(WebSocketSessionContext context) {
    final ProvisioningConnection connection          = new ProvisioningConnection(context.getClient());
    final ProvisioningAddress    provisioningAddress = ProvisioningAddress.generate();

    logger.info("################ WEBSOCKET CONNECT NUMBER "+ provisioningAddress.getNumber() + " DEVICE ID "+provisioningAddress.getDeviceId()+ " ADDRESS "+provisioningAddress.getAddress());
    pubSubManager.subscribe(provisioningAddress, connection);

    context.addListener(new WebSocketSessionContext.WebSocketEventListener() {
      @Override
      public void onWebSocketClose(WebSocketSessionContext context, int statusCode, String reason) {
        pubSubManager.unsubscribe(provisioningAddress, connection);
      }
    });
  }
}
