package com.pgmanagement.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgmanagement.ai.dto.ComplaintAssistRequest;
import com.pgmanagement.ai.dto.ComplaintAssistResponse;
import org.springframework.stereotype.Service;

@Service
public class AIComplaintService {

    private final AIService aiService;
    private final ObjectMapper objectMapper;

    public AIComplaintService(AIService aiService, ObjectMapper objectMapper) {
        this.aiService = aiService;
        this.objectMapper = objectMapper;
    }

    public ComplaintAssistResponse assistComplaint(ComplaintAssistRequest request) {
        String subject = request.getSubject() != null ? request.getSubject().trim() : "";
        String description = request.getDescription() != null ? request.getDescription().trim() : "";
        String fullText = subject + " " + description;

        if (fullText.trim().isEmpty()) {
            return new ComplaintAssistResponse(
                    "OTHER",
                    "LOW",
                    "Please provide complaint details to receive categorization.",
                    "No details provided",
                    subject,
                    false
            );
        }

        // 1. Try Gemini AI
        if (aiService.isConfigured()) {
            try {
                String prompt = "You are an intelligent facilities assistant for a PG accommodation.\n"
                        + "Analyze this tenant complaint:\n"
                        + "Subject: \"" + subject + "\"\n"
                        + "Description: \"" + description + "\"\n\n"
                        + "Return a strict JSON object with these exact keys:\n"
                        + "{\n"
                        + "  \"category\": (one of: \"PLUMBING\", \"ELECTRICITY\", \"CLEANING\", \"FOOD\", \"MAINTENANCE\", \"INTERNET\", \"OTHER\"),\n"
                        + "  \"priority\": (one of: \"HIGH\", \"MEDIUM\", \"LOW\"),\n"
                        + "  \"priorityReason\": (a concise 1-sentence reason for this priority),\n"
                        + "  \"summary\": (a 1-sentence clear summary of the core issue),\n"
                        + "  \"refinedSubject\": (a professional 4-8 word title for the ticket)\n"
                        + "}\n"
                        + "Note: Water leaks, electrical sparks/power cut, lock/security issues are HIGH priority. Cleaning/food queries are MEDIUM or LOW.\n"
                        + "Output ONLY the raw JSON.";

                String aiResult = aiService.generateText(prompt, "You are an expert PG facilities manager.");
                if (aiResult != null && !aiResult.isBlank()) {
                    String cleanJson = aiResult.replaceAll("```json", "").replaceAll("```", "").trim();
                    JsonNode node = objectMapper.readTree(cleanJson);
                    return new ComplaintAssistResponse(
                            node.path("category").asText("MAINTENANCE"),
                            node.path("priority").asText("MEDIUM"),
                            node.path("priorityReason").asText("Standard facility concern requiring review."),
                            node.path("summary").asText(description.length() > 60 ? description.substring(0, 60) + "..." : description),
                            node.path("refinedSubject").asText(subject.isBlank() ? "Maintenance Issue" : subject),
                            true
                    );
                }
            } catch (Exception ignored) {
            }
        }

        // 2. Deterministic Classification & Prioritization Fallback
        return classifyDeterministically(subject, description);
    }

    private ComplaintAssistResponse classifyDeterministically(String subject, String description) {
        String combined = (subject + " " + description).toLowerCase();

        String category = "OTHER";
        String priority = "MEDIUM";
        String priorityReason;
        String refinedSubject = subject.isBlank() ? "Facility Issue" : subject;

        // Categorization rules
        if (combined.contains("tap") || combined.contains("leak") || combined.contains("water")
                || combined.contains("pipe") || combined.contains("flush") || combined.contains("drain")
                || combined.contains("shower") || combined.contains("basin") || combined.contains("geyser")) {
            category = "PLUMBING";
            if (combined.contains("flood") || combined.contains("leak") || combined.contains("no water") || combined.contains("burst")) {
                priority = "HIGH";
                priorityReason = "Water leakages can cause physical property damage and slip hazards.";
            } else {
                priority = "MEDIUM";
                priorityReason = "Plumbing issues affect daily hygiene and require standard maintenance attention.";
            }
        } else if (combined.contains("light") || combined.contains("fan") || combined.contains("power")
                || combined.contains("switch") || combined.contains("spark") || combined.contains("socket")
                || combined.contains("current") || combined.contains("bulb") || combined.contains("electricity")) {
            category = "ELECTRICITY";
            if (combined.contains("spark") || combined.contains("short circuit") || combined.contains("shock") || combined.contains("blackout")) {
                priority = "HIGH";
                priorityReason = "Electrical sparks and short circuits present immediate safety and fire risks.";
            } else {
                priority = "MEDIUM";
                priorityReason = "Electrical appliances require prompt technical inspection.";
            }
        } else if (combined.contains("clean") || combined.contains("dust") || combined.contains("trash")
                || combined.contains("garbage") || combined.contains("broom") || combined.contains("dirty")
                || combined.contains("smell") || combined.contains("stink") || combined.contains("cockroach")) {
            category = "CLEANING";
            priority = "MEDIUM";
            priorityReason = "Cleanliness impacts resident hygiene and health standards.";
        } else if (combined.contains("food") || combined.contains("meal") || combined.contains("lunch")
                || combined.contains("dinner") || combined.contains("breakfast") || combined.contains("taste")
                || combined.contains("cook") || combined.contains("kitchen") || combined.contains("roti")) {
            category = "FOOD";
            priority = combined.contains("stale") || combined.contains("spoiled") || combined.contains("poison") ? "HIGH" : "LOW";
            priorityReason = "Food quality and dietary concerns for residents.";
        } else if (combined.contains("wifi") || combined.contains("wi-fi") || combined.contains("internet")
                || combined.contains("network") || combined.contains("router") || combined.contains("slow net")) {
            category = "INTERNET";
            priority = "MEDIUM";
            priorityReason = "Reliable internet connectivity is vital for study and remote work.";
        } else if (combined.contains("door") || combined.contains("lock") || combined.contains("window")
                || combined.contains("bed") || combined.contains("chair") || combined.contains("table")
                || combined.contains("almirah") || combined.contains("cupboard")) {
            category = "MAINTENANCE";
            if (combined.contains("lock") || combined.contains("broken door")) {
                priority = "HIGH";
                priorityReason = "Door and lock failures directly impact tenant security.";
            } else {
                priority = "LOW";
                priorityReason = "Furniture repair scheduled during normal maintenance hours.";
            }
        } else {
            category = "OTHER";
            priority = "LOW";
            priorityReason = "General non-urgent operational query or request.";
        }

        String summary = description.length() > 80 ? description.substring(0, 80) + "..." : description;

        return new ComplaintAssistResponse(
                category,
                priority,
                priorityReason,
                summary,
                refinedSubject,
                false
        );
    }
}
