/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.processes.interfaces.webapi.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Tyrone Lopez
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Token {

    private String access_token;
    private int expires_in;
    private int refresh_expires_in;
    private String refresh_token;
    private String token_type;
    private int notBeforePolicy;
    private String session_state;
    private String scope;

    @JsonProperty("access_token")
    public String getAccess_token() {
        return this.access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    @JsonProperty("expires_in")
    public int getExpires_in() {
        return this.expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    @JsonProperty("refresh_expires_in")
    public int getRefresh_expires_in() {
        return this.refresh_expires_in;
    }

    public void setRefresh_expires_in(int refresh_expires_in) {
        this.refresh_expires_in = refresh_expires_in;
    }

    @JsonProperty("refresh_token")
    public String getRefresh_token() {
        return this.refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    @JsonProperty("token_type")
    public String getToken_type() {
        return this.token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    @JsonProperty("not-before-policy")
    public int getNotBeforePolicy() {
        return this.notBeforePolicy;
    }

    public void setNotBeforePolicy(int notBeforePolicy) {
        this.notBeforePolicy = notBeforePolicy;
    }

    @JsonProperty("session_state")
    public String getSession_state() {
        return this.session_state;
    }

    public void setSession_state(String session_state) {
        this.session_state = session_state;
    }

    @JsonProperty("scope")
    public String getScope() {
        return this.scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public String toString() {
        return "Token{" + "access_token=" + access_token + ", expires_in=" + expires_in + ", refresh_expires_in=" + refresh_expires_in + ", refresh_token=" + refresh_token + ", token_type=" + token_type + ", notBeforePolicy=" + notBeforePolicy + ", session_state=" + session_state + ", scope=" + scope + '}';
    }

}
