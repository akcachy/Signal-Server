/***** Created By : Ankit Kumar ******/
package org.whispersystems.textsecuregcm.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CachyUserPostResponse {
    @JsonProperty
    private String id;

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
    public List<CachyTaggedUserProfile> taggedUserDetails;
    

    public CachyUserPostResponse() {
    }


    public CachyUserPostResponse(String id, String uuid, String coverImg, String metadata, String url, long createdAt) {
        this.id = id;
        this.uuid = uuid;
        this.coverImg = coverImg;
        this.metadata = metadata;
        this.createdAt = createdAt;
        this.url = url;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
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

    public List<CachyTaggedUserProfile> getTaggedUserDetails() {
        return taggedUserDetails;
    }

    public void setTaggedUserDetails(List<CachyTaggedUserProfile> taggedUserDetails) {
        this.taggedUserDetails = taggedUserDetails;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }
}
