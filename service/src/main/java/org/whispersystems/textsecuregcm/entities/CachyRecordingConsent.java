/***** Created By : Ankit Kumar ******/
package org.whispersystems.textsecuregcm.entities;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CachyRecordingConsent {

    @JsonProperty
    private UUID uuid;
    
    @JsonProperty
    private String callId;
    
    @JsonProperty
    private boolean isRequest;
    
    @JsonProperty
    private boolean consent;
    
    public CachyRecordingConsent() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public boolean isRequest() {
        return isRequest;
    }

    public void setRequest(boolean isRequest) {
        this.isRequest = isRequest;
    }

    public boolean isConsent() {
        return consent;
    }

    public void setConsent(boolean consent) {
        this.consent = consent;
    }
   
}
