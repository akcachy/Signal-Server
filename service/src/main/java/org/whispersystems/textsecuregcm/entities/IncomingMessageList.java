/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package org.whispersystems.textsecuregcm.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class IncomingMessageList {

  @JsonProperty
  @NotNull
  @Valid
  private List<IncomingMessage> messages;

  @JsonProperty
  private long timestamp;

  @JsonProperty
  private boolean online;
  
  @JsonProperty
  private UUID myUUID;

  @JsonProperty
  private boolean isMonetizeMessage;


  public IncomingMessageList() {}

  public List<IncomingMessage> getMessages() {
    return messages;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public boolean isOnline() {
    return online;
  }

  public UUID getMyUUID() {
    return myUUID;
  }

  public void setMyUUID(UUID myUUID) {
    this.myUUID = myUUID;
  }

  public boolean isMonetizeMessage() {
    return isMonetizeMessage;
  }

  public void setMonetizeMessage(boolean isMonetizeMessage) {
    this.isMonetizeMessage = isMonetizeMessage;
  }
}
