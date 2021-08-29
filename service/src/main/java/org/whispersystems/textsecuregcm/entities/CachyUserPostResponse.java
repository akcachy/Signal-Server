/***** Created By : Ankit Kumar ******/
package org.whispersystems.textsecuregcm.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

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
    public long score;
    
    @JsonProperty
    public long likesCount;
    
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
    public  int                             cdnNumber;
    @JsonProperty
    public  String                          remoteId;
    @JsonProperty
    public  Optional<Integer>               size;
    @JsonProperty
    public  Optional<byte[]>                preview;
    @JsonProperty
    public  Optional<String>                fileName;
    @JsonProperty
    public  boolean                         voiceNote;
    @JsonProperty
    public  boolean                         borderless;
    @JsonProperty
    public  boolean                         gif;
    @JsonProperty
    public  int                             width;
    @JsonProperty
    public  int                             height;
    @JsonProperty
    public  Optional<String>                caption;
    @JsonProperty
    public  Optional<String>                blurHash;
    @JsonProperty
    public  long                            uploadTimestamp;
    @JsonProperty
    public  String                          contentType;

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


    public int getCdnNumber() {
        return cdnNumber;
    }


    public void setCdnNumber(int cdnNumber) {
        this.cdnNumber = cdnNumber;
    }


    public String getRemoteId() {
        return remoteId;
    }


    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }


    public Optional<Integer> getSize() {
        return size;
    }


    public void setSize(Optional<Integer> size) {
        this.size = size;
    }


    public Optional<byte[]> getPreview() {
        return preview;
    }


    public void setPreview(Optional<byte[]> preview) {
        this.preview = preview;
    }


    public Optional<String> getFileName() {
        return fileName;
    }


    public void setFileName(Optional<String> fileName) {
        this.fileName = fileName;
    }


    public boolean isVoiceNote() {
        return voiceNote;
    }


    public void setVoiceNote(boolean voiceNote) {
        this.voiceNote = voiceNote;
    }


    public boolean isBorderless() {
        return borderless;
    }


    public void setBorderless(boolean borderless) {
        this.borderless = borderless;
    }


    public boolean isGif() {
        return gif;
    }


    public void setGif(boolean gif) {
        this.gif = gif;
    }


    public int getWidth() {
        return width;
    }


    public void setWidth(int width) {
        this.width = width;
    }


    public int getHeight() {
        return height;
    }


    public void setHeight(int height) {
        this.height = height;
    }


    public Optional<String> getCaption() {
        return caption;
    }


    public void setCaption(Optional<String> caption) {
        this.caption = caption;
    }


    public Optional<String> getBlurHash() {
        return blurHash;
    }


    public void setBlurHash(Optional<String> blurHash) {
        this.blurHash = blurHash;
    }


    public long getUploadTimestamp() {
        return uploadTimestamp;
    }


    public void setUploadTimestamp(long uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }


    public String getContentType() {
        return contentType;
    }


    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
