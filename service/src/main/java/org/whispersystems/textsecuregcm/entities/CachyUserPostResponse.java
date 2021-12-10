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
    public long likesCount;

    @JsonProperty
    public long views;
    
    @JsonProperty
    public long commentsCount;

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

    public CachyUserPostResponse() {
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

    public long getLikesCount() {
        return likesCount;
    }


    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }


    public long getScore() {
        return score;
    }


    public void setScore(long score) {
        this.score = score;
    }


    public long getCommentsCount() {
        return commentsCount;
    }


    public void setCommentsCount(long commentsCount) {
        this.commentsCount = commentsCount;
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


    public long getViews() {
        return views;
    }


    public void setViews(long views) {
        this.views = views;
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



   
}
