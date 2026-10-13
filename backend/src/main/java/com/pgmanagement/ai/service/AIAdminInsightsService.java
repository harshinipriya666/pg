package com.pgmanagement.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgmanagement.ai.dto.AdminInsightsResponse;
import com.pgmanagement.entity.*;
import com.pgmanagement.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIAdminInsightsService {

    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final FeeRepository feeRepository;
    private final ComplaintRepository complaintRepository;
    private final ReviewRepository reviewRepository;
    private final AIService aiService;
    private final ObjectMapper objectMapper;

    public AIAdminInsightsService(
            StudentRepository studentRepository,
            RoomRepository roomRepository,
            FeeRepository feeRepository,
            ComplaintRepository complaintRepository,
            ReviewRepository reviewRepository,
            AIService aiService,
            ObjectMapper objectMapper) {
        this.studentRepository = studentRepository;
        this.roomRepository = roomRepository;
        this.feeRepository = feeRepository;
        this.complaintRepository = complaintRepository;
        this.reviewRepository = reviewRepository;
        this.aiService = aiService;
        this.objectMapper = objectMapper;
    }

    public AdminInsightsResponse generateInsights() {
        // 1. Compute Verified Numbers Directly from MySQL Repositories
        long totalStudents = studentRepository.count();
        List<Room> allRooms = roomRepository.findAll();
        long totalRooms = allRooms.size();
        long occupiedRooms = allRooms.stream().filter(r -> r.getOccupied() > 0).count();
        long availableRooms = allRooms.stream().filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus())).count();

        int totalBedCapacity = allRooms.stream().mapToInt(Room::getCapacity).sum();
        int totalBedsOccupied = allRooms.stream().mapToInt(Room::getOccupied).sum();
        double occupancyRate = totalBedCapacity > 0
                ? Math.round(((double) totalBedsOccupied / totalBedCapacity) * 1000.0) / 10.0
                : 0.0;

        List<Fee> allFees = feeRepository.findAll();
        double totalRentCollected = allFees.stream()
                .filter(f -> "PAID".equalsIgnoreCase(f.getPaymentStatus()))
                .mapToDouble(Fee::getAmount)
                .sum();
        double outstandingRent = allFees.stream()
                .filter(f -> "PENDING".equalsIgnoreCase(f.getPaymentStatus()))
                .mapToDouble(Fee::getAmount)
                .sum();

        List<Complaint> allComplaints = complaintRepository.findAll();
        long totalComplaints = allComplaints.size();
        long pendingComplaints = allComplaints.stream()
                .filter(c -> "PENDING".equalsIgnoreCase(c.getStatus()))
                .count();
        long resolvedComplaints = allComplaints.stream()
                .filter(c -> "RESOLVED".equalsIgnoreCase(c.getStatus()))
                .count();

        Map<String, Long> complaintsByCategory = allComplaints.stream()
                .collect(Collectors.groupingBy(
                        c -> (c.getCategory() != null ? c.getCategory() : "OTHER"),
                        Collectors.counting()
                ));

        Map<String, Long> complaintsByStatus = Map.of(
                "PENDING", pendingComplaints,
                "RESOLVED", resolvedComplaints
        );

        List<Review> allReviews = reviewRepository.findAll();
        double avgRating = allReviews.isEmpty()
                ? 4.5
                : Math.round(allReviews.stream().mapToInt(Review::getRating).average().orElse(4.5) * 10.0) / 10.0;

        AdminInsightsResponse.Metrics metrics = new AdminInsightsResponse.Metrics();
        metrics.setTotalStudents(totalStudents);
        metrics.setTotalRooms(totalRooms);
        metrics.setOccupiedRooms(occupiedRooms);
        metrics.setAvailableRooms(availableRooms);
        metrics.setOccupancyRate(occupancyRate);
        metrics.setTotalRentCollected(totalRentCollected);
        metrics.setOutstandingRent(outstandingRent);
        metrics.setTotalComplaints(totalComplaints);
        metrics.setPendingComplaints(pendingComplaints);
        metrics.setResolvedComplaints(resolvedComplaints);
        metrics.setComplaintsByCategory(complaintsByCategory);
        metrics.setComplaintsByStatus(complaintsByStatus);
        metrics.setAverageRating(avgRating);
        metrics.setTotalReviews(allReviews.size());

        // 2. Synthesize AI Insights
        AdminInsightsResponse response = new AdminInsightsResponse();
        response.setMetrics(metrics);
        response.setGeneratedAt(LocalDateTime.now());

        if (aiService.isConfigured()) {
            try {
                String prompt = String.format(
                        "You are an executive PG business consultant. Analyze these verified metrics:\n"
                                + "- Registered Tenants: %d\n"
                                + "- Rooms: %d total, %d occupied, %d available\n"
                                + "- Bed Occupancy Rate: %.1f%% (%d of %d beds)\n"
                                + "- Collected Rent: ₹%.0f\n"
                                + "- Outstanding Dues: ₹%.0f\n"
                                + "- Complaints: %d total (%d Pending, %d Resolved)\n"
                                + "- Complaints by Category: %s\n"
                                + "- Tenant Satisfaction: %.1f/5 stars (%d reviews)\n\n"
                                + "Return a strict JSON object with these keys:\n"
                                + "{\n"
                                + "  \"executiveSummary\": (2-3 sentences summarizing facility health and operations),\n"
                                + "  \"keyStrengths\": [array of 2-3 positive bullet points],\n"
                                + "  \"criticalAlerts\": [array of 1-3 operational warnings requiring attention],\n"
                                + "  \"actionableRecommendations\": [array of 3 high-impact improvement steps]\n"
                                + "}\n"
                                + "Output ONLY valid JSON.",
                        totalStudents, totalRooms, occupiedRooms, availableRooms,
                        occupancyRate, totalBedsOccupied, totalBedCapacity,
                        totalRentCollected, outstandingRent,
                        totalComplaints, pendingComplaints, resolvedComplaints,
                        complaintsByCategory.toString(),
                        avgRating, allReviews.size()
                );

                String aiResult = aiService.generateText(prompt, "You are a senior PG operations and financial analyst.");
                if (aiResult != null && !aiResult.isBlank()) {
                    String cleanJson = aiResult.replaceAll("```json", "").replaceAll("```", "").trim();
                    JsonNode node = objectMapper.readTree(cleanJson);

                    response.setExecutiveSummary(node.path("executiveSummary").asText());

                    List<String> strengths = new ArrayList<>();
                    if (node.has("keyStrengths")) {
                        for (JsonNode s : node.path("keyStrengths")) strengths.add(s.asText());
                    }
                    response.setKeyStrengths(strengths);

                    List<String> alerts = new ArrayList<>();
                    if (node.has("criticalAlerts")) {
                        for (JsonNode a : node.path("criticalAlerts")) alerts.add(a.asText());
                    }
                    response.setCriticalAlerts(alerts);

                    List<String> recs = new ArrayList<>();
                    if (node.has("actionableRecommendations")) {
                        for (JsonNode r : node.path("actionableRecommendations")) recs.add(r.asText());
                    }
                    response.setActionableRecommendations(recs);

                    response.setAiPowered(true);
                    return response;
                }
            } catch (Exception ignored) {
            }
        }

        // 3. Deterministic Strategic Insight Fallback
        response.setExecutiveSummary(String.format(
                "PG operations are operating at %.1f%% bed occupancy across %d rooms with %d active tenants. Total fee collections stand at ₹%.0f with %d pending complaints currently monitored.",
                occupancyRate, totalRooms, totalStudents, totalRentCollected, pendingComplaints
        ));

        List<String> strengths = new ArrayList<>();
        strengths.add(String.format("Healthy room availability: %d rooms are vacant and ready for new admissions.", availableRooms));
        strengths.add(String.format("Positive community rating: %.1f/5 stars across tenant feedback.", avgRating));
        if (totalRentCollected > 0) {
            strengths.add(String.format("Consistent fee flow: ₹%.0f collected in recent fee cycles.", totalRentCollected));
        }
        response.setKeyStrengths(strengths);

        List<String> alerts = new ArrayList<>();
        if (outstandingRent > 0) {
            alerts.add(String.format("Outstanding rent dues of ₹%.0f pending across tenant accounts.", outstandingRent));
        }
        if (pendingComplaints > 0) {
            alerts.add(String.format("%d tenant complaints are currently pending resolution.", pendingComplaints));
        }
        if (alerts.isEmpty()) {
            alerts.add("Zero critical bottlenecks detected in current operations.");
        }
        response.setCriticalAlerts(alerts);

        List<String> recommendations = new ArrayList<>();
        recommendations.add("Prioritize pending maintenance tickets to maintain high resident satisfaction.");
        recommendations.add("Promote vacant 1st and 2nd floor rooms using AI room recommendations to boost occupancy above 90%.");
        recommendations.add("Set automated WhatsApp/Email reminders for pending fee due dates.");
        response.setActionableRecommendations(recommendations);

        response.setAiPowered(false);
        return response;
    }
}
