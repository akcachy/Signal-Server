/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package org.whispersystems.textsecuregcm.entities;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CachyComment {

  @JsonProperty
  private UUID id;
  
  @JsonProperty
  private UUID uuid;
  
  @JsonProperty
  private String cmt;
  
  @JsonProperty
  private long ct;

  public CachyComment() {
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  public String getCmt() {
    return cmt;
  }

  public void setCmt(String cmt) {
    this.cmt = cmt;
  }

  public long getCt() {
    return ct;
  }

  public void setCt(long ct) {
    this.ct = ct;
  }

  
}
