package com.pgmanagement.ai.dto;

import java.util.List;

public class RoomRecommendationRequest {

    private Double maxBudget;
    private String sharingPreference; // "Single", "Double", "Triple", "Any"
    private Integer floorPreference;
    private List<String> requiredFacilities;
    private String moveInDate;

    public RoomRecommendationRequest() {
    }

    public Double getMaxBudget() {
        return maxBudget;
    }

    public void setMaxBudget(Double maxBudget) {
        this.maxBudget = maxBudget;
    }

    public String getSharingPreference() {
        return sharingPreference;
    }

    public void setSharingPreference(String sharingPreference) {
        this.sharingPreference = sharingPreference;
    }

    public Integer getFloorPreference() {
        return floorPreference;
    }

    public void setFloorPreference(Integer floorPreference) {
        this.floorPreference = floorPreference;
    }

    public List<String> getRequiredFacilities() {
        return requiredFacilities;
    }

    public void setRequiredFacilities(List<String> requiredFacilities) {
        this.requiredFacilities = requiredFacilities;
    }

    public String getMoveInDate() {
        return moveInDate;
    }

    public void setMoveInDate(String moveInDate) {
        this.moveInDate = moveInDate;
    }
}
