/***** Created By : Ankit Kumar ******/
package org.whispersystems.textsecuregcm.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CachyUserPostResponse {
    @JsonProperty
    private String postId;

    @JsonProperty
    private String uuid;


    @JsonProperty
    public String metadata;
    
    @JsonProperty
    public long createdAt;
    
    
    @JsonProperty
    public long score;
    
    @JsonProperty
    public String likesCount;

    @JsonProperty
    public String views;
    
    @JsonProperty
    public String commentsCount;

    @JsonProperty
    public long duration;

    @JsonProperty
    public String description;
    
    @JsonProperty
    public boolean isLiked;
    
    @JsonProperty
    public List<CachyTaggedUserProfile> contributorsDetails;
    
    @JsonProperty
    public SignalServiceAttachmentPointer video;
    
    @JsonProperty
    public SignalServiceAttachmentPointer coverImg;

    @JsonIgnore
    public int ageGroup;

    @JsonIgnore
    public int category;

    @JsonProperty
    public String firstName;

    @JsonProperty
    public String lastName;

    @JsonProperty
    public int dirtyFlag;

    @JsonIgnore
    public boolean isStory;

    public CachyUserPostResponse() {
        this.likesCount = "0";
        this.views = "0";
        this.commentsCount = "0";
    }

  public CachyUserPostResponse(CachyUserPostResponse post) {


    this.postId = post.getPostId();
    this.uuid = post.getUuid();
    this.metadata = post.getMetadata();
    this.createdAt = post.getCreatedAt();
    this.score = post.getScore();
    this.likesCount = post.getLikesCount();
    this.views = post.getViews();
    this.commentsCount = post.getCommentsCount();
    this.duration = post.getDuration();
    this.description = post.getDescription();
    this.isLiked = post.isLiked();
    this.contributorsDetails = post.getContributorsDetails();
    this.video = post.getVideo();
    this.coverImg = post.getCoverImg();
    this.ageGroup = post.getAgeGroup();
    this.category = post.getCategory();
    this.firstName = post.getFirstName();
    this.lastName = post.getLastName();
    this.dirtyFlag = post.getDirtyFlag();
  }

  public String getPostId() {
        return postId;
    }


    public void setPostId(String postId) {
        this.postId = postId;
    }


    public String getUuid() {
        return uuid;
    }


    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public String getMetadata() {
        return metadata;
    }


    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }


    public long getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getLikesCount() {
        return likesCount;
    }


    public void setLikesCount(String likesCount) {
        this.likesCount = likesCount;
    }


    public long getScore() {
        return score;
    }


    public void setScore(long score) {
        this.score = score;
    }


    


    public boolean isLiked() {
        return isLiked;
    }


    public void setLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }


    public long getDuration() {
        return duration;
    }


    public void setDuration(long duration) {
        this.duration = duration;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public List<CachyTaggedUserProfile> getContributorsDetails() {
        return contributorsDetails;
    }


    public void setContributorsDetails(List<CachyTaggedUserProfile> contributorsDetails) {
        this.contributorsDetails = contributorsDetails;
    }


    public SignalServiceAttachmentPointer getVideo() {
        return video;
    }


    public void setVideo(SignalServiceAttachmentPointer video) {
        this.video = video;
    }


    public SignalServiceAttachmentPointer getCoverImg() {
        return coverImg;
    }


    public void setCoverImg(SignalServiceAttachmentPointer coverImg) {
        this.coverImg = coverImg;
    }


    public String getViews() {
        return views;
    }


    public void setViews(String views) {
        this.views = views;
    }


    public String getCommentsCount() {
        return commentsCount;
    }


    public void setCommentsCount(String commentsCount) {
        this.commentsCount = commentsCount;
    }


    public int getAgeGroup() {
        return ageGroup;
    }


    public void setAgeGroup(int ageGroup) {
        this.ageGroup = ageGroup;
    }


    public int getCategory() {
        return category;
    }


    public void setCategory(int category) {
        this.category = category;
    }


    public String getFirstName() {
        return firstName;
    }


    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }


    public String getLastName() {
        return lastName;
    }


    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public int getDirtyFlag() {
        return dirtyFlag;
    }


    public void setDirtyFlag(int dirtyFlag) {
        this.dirtyFlag = dirtyFlag;
    }


    public boolean isStory() {
        return isStory;
    }


    public void setStory(boolean isStory) {
        this.isStory = isStory;
    }



   
}
