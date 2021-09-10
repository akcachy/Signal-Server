/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.websocket;

import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.auth.AccountAuthenticator;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.websocket.auth.WebSocketAuthenticator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.dropwizard.auth.basic.BasicCredentials;


public class WebSocketAccountAuthenticator implements WebSocketAuthenticator<Account> {

  private final AccountAuthenticator accountAuthenticator;

  private final Logger         logger                           = LoggerFactory.getLogger(WebSocketAccountAuthenticator.class);
  public WebSocketAccountAuthenticator(AccountAuthenticator accountAuthenticator) {
    this.accountAuthenticator = accountAuthenticator;
  }

  @Override
  public AuthenticationResult<Account> authenticate(UpgradeRequest request) {
    Map<String, List<String>> parameters = request.getParameterMap();
    List<String>              usernames  = parameters.get("login");
    List<String>              passwords  = parameters.get("password");

    logger.info("################ WEBSOCKET AUTHENTICATOR HOST "+ request.getHost());;
    logger.info("################ WEBSOCKET AUTHENTICATOR ");
    if (usernames == null || usernames.size() == 0 ||
        passwords == null || passwords.size() == 0)
    {
      return new AuthenticationResult<>(Optional.empty(), false);
    }

    BasicCredentials credentials = new BasicCredentials(usernames.get(0).replace(" ", "+"),
                                                        passwords.get(0).replace(" ", "+"));

    logger.info("############## WEBSOCKET AUTHENTICATOR BASIC CRED "+ credentials.getUsername() + " PASSWORD "+ credentials.getPassword());
    return new AuthenticationResult<>(accountAuthenticator.authenticate(credentials), true);
  }

}
