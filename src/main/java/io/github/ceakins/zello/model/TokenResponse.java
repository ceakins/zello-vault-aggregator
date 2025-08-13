package io.github.ceakins.zello.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenResponse {
    private final String status;
    private final String token;
    private final String sid;

    public TokenResponse(
            @JsonProperty("status") String status,
            @JsonProperty("token") String token,
            @JsonProperty("sid") String sid) {
        this.status = status;
        this.token = token;
        this.sid = sid;
    }

    // --- GETTERS ---
    public String getStatus() { return status; }
    public String getToken() { return token; }
    public String getSid() { return sid; }
}