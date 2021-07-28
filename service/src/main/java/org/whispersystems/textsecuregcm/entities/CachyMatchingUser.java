/***** Created By : Ankit Kumar ******/
package org.whispersystems.textsecuregcm.entities;

import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CachyMatchingUser {
    @JsonProperty
    private UUID uuid;

    @JsonProperty
    private Set<Long> matchingKeyword;

    @JsonProperty
    private boolean isCaller;

    @JsonProperty
    private String number;



    public CachyMatchingUser() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Set<Long> getMatchingKeyword() {
        return matchingKeyword;
    }

    public void setMatchingKeyword(Set<Long> matchingKeyword) {
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
    
}
