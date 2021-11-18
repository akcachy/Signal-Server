/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TurnToken {

  @JsonProperty
  private String username;

  @JsonProperty
  private String password;

  @JsonProperty
  private List<String> urls;

  @JsonProperty
  private boolean turnOnly;

  public TurnToken(String username, String password, List<String> urls) {
    this.username = username;
    this.password = password;
    this.urls     = urls;
  }

  public boolean isTurnOnly() {
    return turnOnly;
  }

  public void setTurnOnly(boolean turnOnly) {
    this.turnOnly = turnOnly;
  }
  
}
