package com.pgmanagement.ai.dto;

import com.pgmanagement.entity.Room;
import java.util.List;
import java.util.Map;

public class RoomSearchResponse {

    private String originalQuery;
    private Map<String, Object> extractedCriteria; // e.g. { "maxBudget": 7000, "capacity": 2, "facilities": ["Wi-Fi"] }
    private List<Room> matchingRooms;
    private String searchExplanation;
    private boolean aiPowered;

    public RoomSearchResponse() {
    }

    public RoomSearchResponse(String originalQuery, Map<String, Object> extractedCriteria, List<Room> matchingRooms, String searchExplanation, boolean aiPowered) {
        this.originalQuery = originalQuery;
        this.extractedCriteria = extractedCriteria;
        this.matchingRooms = matchingRooms;
        this.searchExplanation = searchExplanation;
        this.aiPowered = aiPowered;
    }

    public String getOriginalQuery() {
        return originalQuery;
    }

    public void setOriginalQuery(String originalQuery) {
        this.originalQuery = originalQuery;
    }

    public Map<String, Object> getExtractedCriteria() {
        return extractedCriteria;
    }

    public void setExtractedCriteria(Map<String, Object> extractedCriteria) {
        this.extractedCriteria = extractedCriteria;
    }

    public List<Room> getMatchingRooms() {
        return matchingRooms;
    }

    public void setMatchingRooms(List<Room> matchingRooms) {
        this.matchingRooms = matchingRooms;
    }

    public String getSearchExplanation() {
        return searchExplanation;
    }

    public void setSearchExplanation(String searchExplanation) {
        this.searchExplanation = searchExplanation;
    }

    public boolean isAiPowered() {
        return aiPowered;
    }

    public void setAiPowered(boolean aiPowered) {
        this.aiPowered = aiPowered;
    }
}
