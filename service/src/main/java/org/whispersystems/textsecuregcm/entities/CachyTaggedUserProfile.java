/***** Created By : Ankit Kumar ******/
package org.whispersystems.textsecuregcm.entities;

import com.fasterxml.jackson.annotation.JsonProperty;


public class CachyTaggedUserProfile {

    @JsonProperty
    private String uuid;

    @JsonProperty
    private String fn;

    @JsonProperty
    private String ln;

    @JsonProperty
    private String un;
    
    @JsonProperty
    private String num;
    
    @JsonProperty
    private String ua;

    @JsonProperty
    private String pk;
    
    @JsonProperty
    private int pro;
    
    @JsonProperty
    private int pre;

    @JsonProperty
    private boolean isUserStoryExists;

    public CachyTaggedUserProfile(){

    }


    public CachyTaggedUserProfile(String uuid, String fn, String ln, String un, String num, String ua, String pk) {
        this.uuid = uuid;
        this.fn = fn;
        this.ln = ln;
        this.un = un;
        this.num = num;
        this.ua = ua;
        this.pk = pk;
    }


    public String getUuid() {
        return uuid;
    }


    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public String getFn() {
        return fn;
    }


    public void setFn(String fn) {
        this.fn = fn;
    }


    public String getLn() {
        return ln;
    }


    public void setLn(String ln) {
        this.ln = ln;
    }


    public String getUn() {
        return un;
    }


    public void setUn(String un) {
        this.un = un;
    }


    public String getUa() {
        return ua;
    }


    public void setUa(String ua) {
        this.ua = ua;
    }


    public String getPk() {
        return pk;
    }


    public void setPk(String pk) {
        this.pk = pk;
    }


    public int isPro() {
        return pro;
    }


    public void setPro(int pro) {
        this.pro = pro;
    }


    public int isPre() {
        return pre;
    }


    public void setPre(int pre) {
        this.pre = pre;
    }


    public String getNum() {
        return num;
    }


    public void setNum(String num) {
        this.num = num;
    }


    public boolean isUserStoryExists() {
        return isUserStoryExists;
    }


    public void setUserStoryExists(boolean isUserStoryExists) {
        this.isUserStoryExists = isUserStoryExists;
    }



    
}