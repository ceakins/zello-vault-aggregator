package io.github.ceakins.zello.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaResponse {
    private final String status;
    private final String url;
    private final int progress;

    public MediaResponse(
            @JsonProperty("status") String status,
            @JsonProperty("url") String url,
            @JsonProperty("progress") int progress) {
        this.status = status;
        this.url = url;
        this.progress = progress;
    }

    // --- GETTERS ADDED ---
    public String getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public int getProgress() {
        return progress;
    }
}