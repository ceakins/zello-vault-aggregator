package com.charles.eakins.zello.model;

public class ProcessedMessage {
    private final String displayName;
    private final String time24h;
    private final String mediaKey;
    private final String transcription;
    private final long rawTimestamp; // ADDED

    public ProcessedMessage(String displayName, String time24h, String mediaKey, String transcription, long rawTimestamp) { //MODIFIED
        this.displayName = displayName;
        this.time24h = time24h;
        this.mediaKey = mediaKey;
        this.transcription = transcription;
        this.rawTimestamp = rawTimestamp; // ADDED
    }

    public String getDisplayName() { return displayName; }
    public String getTime24h() { return time24h; }
    public String getMediaKey() { return mediaKey; }
    public String getTranscription() { return transcription; }
    public long getRawTimestamp() { return rawTimestamp; } // ADDED
}