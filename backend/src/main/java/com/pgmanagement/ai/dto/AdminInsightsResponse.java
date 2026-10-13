package com.pgmanagement.ai.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AdminInsightsResponse {

    public static class Metrics {
        private long totalStudents;
        private long totalRooms;
        private long occupiedRooms;
        private long availableRooms;
        private double occupancyRate;
        private double totalRentCollected;
        private double outstandingRent;
        private long totalComplaints;
        private long pendingComplaints;
        private long resolvedComplaints;
        private Map<String, Long> complaintsByCategory;
        private Map<String, Long> complaintsByStatus;
        private double averageRating;
        private long totalReviews;

        public Metrics() {
        }

        public long getTotalStudents() {
            return totalStudents;
        }

        public void setTotalStudents(long totalStudents) {
            this.totalStudents = totalStudents;
        }

        public long getTotalRooms() {
            return totalRooms;
        }

        public void setTotalRooms(long totalRooms) {
            this.totalRooms = totalRooms;
        }

        public long getOccupiedRooms() {
            return occupiedRooms;
        }

        public void setOccupiedRooms(long occupiedRooms) {
            this.occupiedRooms = occupiedRooms;
        }

        public long getAvailableRooms() {
            return availableRooms;
        }

        public void setAvailableRooms(long availableRooms) {
            this.availableRooms = availableRooms;
        }

        public double getOccupancyRate() {
            return occupancyRate;
        }

        public void setOccupancyRate(double occupancyRate) {
            this.occupancyRate = occupancyRate;
        }

        public double getTotalRentCollected() {
            return totalRentCollected;
        }

        public void setTotalRentCollected(double totalRentCollected) {
            this.totalRentCollected = totalRentCollected;
        }

        public double getOutstandingRent() {
            return outstandingRent;
        }

        public void setOutstandingRent(double outstandingRent) {
            this.outstandingRent = outstandingRent;
        }

        public long getTotalComplaints() {
            return totalComplaints;
        }

        public void setTotalComplaints(long totalComplaints) {
            this.totalComplaints = totalComplaints;
        }

        public long getPendingComplaints() {
            return pendingComplaints;
        }

        public void setPendingComplaints(long pendingComplaints) {
            this.pendingComplaints = pendingComplaints;
        }

        public long getResolvedComplaints() {
            return resolvedComplaints;
        }

        public void setResolvedComplaints(long resolvedComplaints) {
            this.resolvedComplaints = resolvedComplaints;
        }

        public Map<String, Long> getComplaintsByCategory() {
            return complaintsByCategory;
        }

        public void setComplaintsByCategory(Map<String, Long> complaintsByCategory) {
            this.complaintsByCategory = complaintsByCategory;
        }

        public Map<String, Long> getComplaintsByStatus() {
            return complaintsByStatus;
        }

        public void setComplaintsByStatus(Map<String, Long> complaintsByStatus) {
            this.complaintsByStatus = complaintsByStatus;
        }

        public double getAverageRating() {
            return averageRating;
        }

        public void setAverageRating(double averageRating) {
            this.averageRating = averageRating;
        }

        public long getTotalReviews() {
            return totalReviews;
        }

        public void setTotalReviews(long totalReviews) {
            this.totalReviews = totalReviews;
        }
    }

    private Metrics metrics;
    private String executiveSummary;
    private List<String> keyStrengths;
    private List<String> criticalAlerts;
    private List<String> actionableRecommendations;
    private boolean aiPowered;
    private LocalDateTime generatedAt = LocalDateTime.now();

    public AdminInsightsResponse() {
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    public String getExecutiveSummary() {
        return executiveSummary;
    }

    public void setExecutiveSummary(String executiveSummary) {
        this.executiveSummary = executiveSummary;
    }

    public List<String> getKeyStrengths() {
        return keyStrengths;
    }

    public void setKeyStrengths(List<String> keyStrengths) {
        this.keyStrengths = keyStrengths;
    }

    public List<String> getCriticalAlerts() {
        return criticalAlerts;
    }

    public void setCriticalAlerts(List<String> criticalAlerts) {
        this.criticalAlerts = criticalAlerts;
    }

    public List<String> getActionableRecommendations() {
        return actionableRecommendations;
    }

    public void setActionableRecommendations(List<String> actionableRecommendations) {
        this.actionableRecommendations = actionableRecommendations;
    }

    public boolean isAiPowered() {
        return aiPowered;
    }

    public void setAiPowered(boolean aiPowered) {
        this.aiPowered = aiPowered;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
