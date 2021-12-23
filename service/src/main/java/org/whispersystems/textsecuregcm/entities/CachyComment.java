/***** Created By : Ankit Kumar ******/
package org.whispersystems.textsecuregcm.entities;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.bson.types.ObjectId;

public class CachyComment {

  @JsonProperty
  private ObjectId _id;
  
  @JsonProperty
  private String postId;

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

  public ObjectId getId() {
    return _id;
  }

  public void setId(ObjectId _id) {
    this._id = _id;
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

  public int getPro() {
    return pro;
  }

  public void setPro(int pro) {
    this.pro = pro;
  }

  public int getPre() {
    return pre;
  }

  public void setPre(int pre) {
    this.pre = pre;
  }

  public boolean isUserStoryExists() {
    return isUserStoryExists;
  }

  public void setUserStoryExists(boolean isUserStoryExists) {
    this.isUserStoryExists = isUserStoryExists;
  }

  public String getPostId() {
    return postId;
  }

  public void setPostId(String postId) {
    this.postId = postId;
  }

  
}
