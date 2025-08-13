package io.github.ceakins.zello.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    private final String status;
    private final String code;

    public LoginResponse(@JsonProperty("status") String status, @JsonProperty("code") String code) {
        this.status = status;
        this.code = code;
    }

    // --- GETTERS ---
    public String getStatus() { return status; }
    public String getCode() { return code; }
}