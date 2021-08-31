/***** Created By : Ankit Kumar ******/
package org.whispersystems.textsecuregcm.entities;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignalServiceAttachmentPointer {
    @JsonProperty
    public  int                             cdnNumber;
    
    @JsonProperty
    public  String                          remoteid;
    
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
    
    @JsonProperty
    public  String                          url;

    public SignalServiceAttachmentPointer() {
    }

    public int getCdnNumber() {
        return cdnNumber;
    }

    public void setCdnNumber(int cdnNumber) {
        this.cdnNumber = cdnNumber;
    }

    public String getRemoteId() {
        return remoteid;
    }

    public void setRemoteId(String remoteid) {
        this.remoteid = remoteid;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
}
