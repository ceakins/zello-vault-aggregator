package io.github.ceakins.zello.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZelloUser {
    private final String username;
    private final String fullName;

    public ZelloUser(@JsonProperty("name") String username, @JsonProperty("full_name") String fullName) {
        this.username = username;
        this.fullName = fullName;
    }

    // --- GETTERS ---
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
}