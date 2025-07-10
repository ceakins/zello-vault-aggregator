package com.charles.eakins.zello.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserListResponse {
    private final String status;
    private final List<ZelloUser> users;

    public UserListResponse(@JsonProperty("status") String status, @JsonProperty("users") List<ZelloUser> users) {
        this.status = status;
        this.users = users;
    }

    // --- GETTERS ---
    public String getStatus() { return status; }
    public List<ZelloUser> getUsers() { return users; }
}
