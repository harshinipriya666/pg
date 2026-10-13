package com.pgmanagement.ai.dto;

import com.pgmanagement.entity.Room;
import java.util.List;

public class RoomRecommendationResponse {

    public static class RankedRoom {
        private Room room;
        private int matchScore; // e.g. 95%
        private String reason; // AI-generated rationale

        public RankedRoom() {
        }

        public RankedRoom(Room room, int matchScore, String reason) {
            this.room = room;
            this.matchScore = matchScore;
            this.reason = reason;
        }

        public Room getRoom() {
            return room;
        }

        public void setRoom(Room room) {
            this.room = room;
        }

        public int getMatchScore() {
            return matchScore;
        }

        public void setMatchScore(int matchScore) {
            this.matchScore = matchScore;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    private List<RankedRoom> recommendations;
    private String summary;
    private boolean aiPowered;

    public RoomRecommendationResponse() {
    }

    public RoomRecommendationResponse(List<RankedRoom> recommendations, String summary, boolean aiPowered) {
        this.recommendations = recommendations;
        this.summary = summary;
        this.aiPowered = aiPowered;
    }

    public List<RankedRoom> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<RankedRoom> recommendations) {
        this.recommendations = recommendations;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public boolean isAiPowered() {
        return aiPowered;
    }

    public void setAiPowered(boolean aiPowered) {
        this.aiPowered = aiPowered;
    }
}
