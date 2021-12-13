/***** Created By : Ankit Kumar ******/
package org.whispersystems.textsecuregcm.entities;

import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CachyMatchingUser {
    @JsonProperty
    private String uuid;

    @JsonProperty
    private Set<Integer> matchingKeyword;

    @JsonProperty
    private boolean isCaller;
    
    @JsonProperty
    private boolean followEnable;

    @JsonProperty
    private String number;
    
    @JsonProperty
    private String callId;

    @JsonProperty
    private byte[] unidentifiedAccess;
    
    @JsonProperty
    private String profileKey;

    public CachyMatchingUser() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Set<Integer> getMatchingKeyword() {
        return matchingKeyword;
    }

    public void setMatchingKeyword(Set<Integer> matchingKeyword) {
        this.matchingKeyword = matchingKeyword;
    }

    public boolean isCaller() {
        return isCaller;
    }

    public void setCaller(boolean isCaller) {
        this.isCaller = isCaller;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public boolean isFollowEnable() {
        return followEnable;
    }

    public void setFollowEnable(boolean followEnable) {
        this.followEnable = followEnable;
    }

    public byte[] getUnidentifiedAccess() {
        return unidentifiedAccess;
    }

    public void setUnidentifiedAccess(byte[] unidentifiedAccess) {
        this.unidentifiedAccess = unidentifiedAccess;
    }

    public String getProfileKey() {
        return profileKey;
    }

    public void setProfileKey(String profileKey) {
        this.profileKey = profileKey;
    }
    
}
