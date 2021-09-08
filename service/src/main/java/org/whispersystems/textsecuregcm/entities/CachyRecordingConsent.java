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
    private String type;
    
    
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
   
}
