/***** Created By : Ankit Kumar ******/
package org.whispersystems.textsecuregcm.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CachyUserPostMonetizationResponse extends CachyUserPostResponse {
    @JsonProperty
    private String monetization;

  public CachyUserPostMonetizationResponse(String monetization) {
    this.monetization = monetization;
  }
  public CachyUserPostMonetizationResponse(String monetization, CachyUserPostResponse post ) {
    super(post);
    this.monetization = monetization;
  }

  public String getMonetization() {
    return monetization;
  }

  public void setMonetization(String monetization) {
    this.monetization = monetization;
  }
}
