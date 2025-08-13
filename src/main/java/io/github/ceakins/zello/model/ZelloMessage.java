package io.github.ceakins.zello.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZelloMessage {
    private final String sender;
    private final long timestamp;
    private final String mediaKey;
    private final String transcription;

    public ZelloMessage(
            @JsonProperty("sender") String sender,
            @JsonProperty("ts") long timestamp,
            @JsonProperty("media_key") String mediaKey,
            @JsonProperty("transcription") String transcription) {
        this.sender = sender;
        this.timestamp = timestamp;
        this.mediaKey = mediaKey;
        this.transcription = transcription;
    }

    // --- Getters are required for other classes to access these fields ---
    public String getSender() { return sender; }
    public long getTimestamp() { return timestamp; }
    public String getMediaKey() { return mediaKey; }
    public String getTranscription() { return transcription; }
}