/***** Created By : Ankit Kumar ******/
package org.whispersystems.textsecuregcm.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CachyCommentRes {

  @JsonProperty
  private String id;
  
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
  public boolean isLiked;

  @JsonProperty
  public String likesCount;

  @JsonProperty
  private boolean isUserStoryExists;

  @JsonProperty
  private List<String> reply = new ArrayList<>();
  

  @JsonProperty
  private List<String> stories = new ArrayList<>();
  
  public CachyCommentRes() {
    this.likesCount = "0";
    this.stories = new ArrayList<>();
  }

  public CachyCommentRes(String id, String postId, UUID uuid, String cmt, long ct, String fn, String ln, String un,
      String ua, String pk, int pro, int pre, boolean isLiked, String likesCount, boolean isUserStoryExists) {
    this.id = id;
    this.postId = postId;
    this.uuid = uuid;
    this.cmt = cmt;
    this.ct = ct;
    this.fn = fn;
    this.ln = ln;
    this.un = un;
    this.ua = ua;
    this.pk = pk;
    this.pro = pro;
    this.pre = pre;
    this.isLiked = isLiked;
    this.likesCount = likesCount;
    this.isUserStoryExists = isUserStoryExists;
  }

  public String getId() {
    return id;
  }



  public void setId(String id) {
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


  public List<String> getReply() {
    return reply;
  }

  public void setReply(List<String> reply) {
    this.reply = reply;
  }

  public void setStories(List<String> stories) {
    this.stories = stories;
  }

  public boolean isLiked() {
    return isLiked;
  }



  public void setLiked(boolean isLiked) {
    this.isLiked = isLiked;
  }



  public String getLikesCount() {
    return likesCount;
  }



  public void setLikesCount(String likesCount) {
    this.likesCount = likesCount;
  }

  public List<String> getStories() {
    return stories;
  }
}

