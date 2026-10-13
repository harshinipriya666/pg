package com.pgmanagement.ai.dto;

public class ComplaintAssistResponse {

    private String suggestedCategory; // MAINTENANCE, PLUMBING, ELECTRICITY, CLEANING, FOOD, INTERNET, OTHER
    private String suggestedPriority; // LOW, MEDIUM, HIGH
    private String priorityReason;
    private String summary;
    private String refinedSubject;
    private boolean aiPowered;

    public ComplaintAssistResponse() {
    }

    public ComplaintAssistResponse(String suggestedCategory, String suggestedPriority, String priorityReason, String summary, String refinedSubject, boolean aiPowered) {
        this.suggestedCategory = suggestedCategory;
        this.suggestedPriority = suggestedPriority;
        this.priorityReason = priorityReason;
        this.summary = summary;
        this.refinedSubject = refinedSubject;
        this.aiPowered = aiPowered;
    }

    public String getSuggestedCategory() {
        return suggestedCategory;
    }

    public void setSuggestedCategory(String suggestedCategory) {
        this.suggestedCategory = suggestedCategory;
    }

    public String getSuggestedPriority() {
        return suggestedPriority;
    }

    public void setSuggestedPriority(String suggestedPriority) {
        this.suggestedPriority = suggestedPriority;
    }

    public String getPriorityReason() {
        return priorityReason;
    }

    public void setPriorityReason(String priorityReason) {
        this.priorityReason = priorityReason;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getRefinedSubject() {
        return refinedSubject;
    }

    public void setRefinedSubject(String refinedSubject) {
        this.refinedSubject = refinedSubject;
    }

    public boolean isAiPowered() {
        return aiPowered;
    }

    public void setAiPowered(boolean aiPowered) {
        this.aiPowered = aiPowered;
    }
}
