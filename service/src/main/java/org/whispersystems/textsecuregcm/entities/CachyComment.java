/***** Created By : Ankit Kumar ******/
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

  @JsonProperty
  private String fn;

  @JsonProperty
  private String ln;

  @JsonProperty
  private String un;

  @JsonProperty
  private String ua;

  @JsonProperty
  private String pk;

  @JsonProperty
  private int pro;

  @JsonProperty
  private int pre;

  @JsonProperty
  private boolean isUserStoryExists;


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

  public String getFn() {
    return fn;
  }

  public void setFn(String fn) {
    this.fn = fn;
  }

  public String getLn() {
    return ln;
  }

  public void setLn(String ln) {
    this.ln = ln;
  }

  public String getUn() {
    return un;
  }

  public void setUn(String un) {
    this.un = un;
  }

  public String getUa() {
    return ua;
  }

  public void setUa(String ua) {
    this.ua = ua;
  }

  public String getPk() {
    return pk;
  }

  public void setPk(String pk) {
    this.pk = pk;
  }

  
}
