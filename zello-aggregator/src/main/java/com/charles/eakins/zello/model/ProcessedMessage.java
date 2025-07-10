package com.charles.eakins.zello.model;

/**
 * A Data Transfer Object (DTO) representing a single, fully-processed message
 * ready for display in the view.
 */
public class ProcessedMessage {
    private final String displayName;
    private final String time24h;
    private final String mediaKey;
    private final String transcription;

    public ProcessedMessage(String displayName, String time24h, String mediaKey, String transcription) {
        this.displayName = displayName;
        this.time24h = time24h;
        this.mediaKey = mediaKey;
        this.transcription = transcription;
    }

    // Getters for Thymeleaf to access the data
    public String getDisplayName() {
        return displayName;
    }

    public String getTime24h() {
        return time24h;
    }

    public String getMediaKey() {
        return mediaKey;
    }

    public String getTranscription() {
        return transcription;
    }
}
