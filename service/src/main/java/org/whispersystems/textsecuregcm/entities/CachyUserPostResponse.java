/***** Created By : Ankit Kumar ******/
package org.whispersystems.textsecuregcm.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CachyUserPostResponse {
    @JsonProperty
    private String postId;

    @JsonIgnore
    private String uuid;
 
    @JsonProperty
    public String coverImg;

    @JsonProperty
    public String metadata;
    
    @JsonProperty
    public long createdAt;
    
    @JsonProperty
    public String url;
    
    @JsonProperty
    public long seq;
    
    @JsonProperty
    public long likes;
    
    @JsonProperty
    public long comments;

    @JsonProperty
    public long duration;

    @JsonProperty
    public String description;
    
    @JsonProperty
    public boolean isLiked;
    
    @JsonProperty
    public List<CachyTaggedUserProfile> contributorsDetails;
    

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


    public String getCoverImg() {
        return coverImg;
    }


    public void setCoverImg(String coverImg) {
        this.coverImg = coverImg;
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


    public String getUrl() {
        return url;
    }


    public void setUrl(String url) {
        this.url = url;
    }


    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }


    public long getLikes() {
        return likes;
    }


    public void setLikes(long likes) {
        this.likes = likes;
    }


    public long getComments() {
        return comments;
    }


    public void setComments(long comments) {
        this.comments = comments;
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
}
