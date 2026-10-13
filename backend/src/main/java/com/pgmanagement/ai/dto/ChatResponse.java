package com.pgmanagement.ai.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ChatResponse {

    private String reply;
    private boolean aiPowered;
    private List<String> suggestions;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ChatResponse() {
    }

    public ChatResponse(String reply, boolean aiPowered, List<String> suggestions) {
        this.reply = reply;
        this.aiPowered = aiPowered;
        this.suggestions = suggestions;
        this.timestamp = LocalDateTime.now();
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public boolean isAiPowered() {
        return aiPowered;
    }

    public void setAiPowered(boolean aiPowered) {
        this.aiPowered = aiPowered;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
