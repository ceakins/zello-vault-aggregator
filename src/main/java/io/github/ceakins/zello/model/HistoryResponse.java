package io.github.ceakins.zello.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoryResponse {
    private final String status;
    private final int total;
    private final List<ZelloMessage> messages;

    public HistoryResponse(
            @JsonProperty("status") String status,
            @JsonProperty("total") int total,
            @JsonProperty("messages") List<ZelloMessage> messages) {
        this.status = status;
        this.total = total;
        this.messages = messages;
    }

    // --- GETTERS ADDED ---
    public String getStatus() {
        return status;
    }

    public int getTotal() {
        return total;
    }

    public List<ZelloMessage> getMessages() {
        return messages;
    }
}