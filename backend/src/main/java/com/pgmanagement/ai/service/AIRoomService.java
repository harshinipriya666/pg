package com.pgmanagement.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgmanagement.ai.dto.*;
import com.pgmanagement.entity.Room;
import com.pgmanagement.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AIRoomService {

    private final RoomRepository roomRepository;
    private final AIService aiService;
    private final ObjectMapper objectMapper;

    public AIRoomService(RoomRepository roomRepository, AIService aiService, ObjectMapper objectMapper) {
        this.roomRepository = roomRepository;
        this.aiService = aiService;
        this.objectMapper = objectMapper;
    }

    public RoomRecommendationResponse recommendRooms(RoomRecommendationRequest request) {
        List<Room> availableRooms = roomRepository.findByStatus("AVAILABLE");

        if (availableRooms.isEmpty()) {
            return new RoomRecommendationResponse(
                    Collections.emptyList(),
                    "No rooms are currently available for allocation. All PG rooms are at full capacity.",
                    false
            );
        }

        Double budget = request.getMaxBudget() != null ? request.getMaxBudget() : 15000.0;
        String sharingPref = request.getSharingPreference() != null ? request.getSharingPreference().trim().toLowerCase() : "any";
        Integer targetCapacity = getCapacityFromSharingPreference(sharingPref);
        Integer floorPref = request.getFloorPreference();
        List<String> facilitiesPref = request.getRequiredFacilities() != null ? request.getRequiredFacilities() : Collections.emptyList();

        List<RoomRecommendationResponse.RankedRoom> rankedList = new ArrayList<>();

        for (Room room : availableRooms) {
            int score = 0;
            double rent = room.getRent();

            // 1. Budget Score (Up to 40 pts)
            if (rent <= budget) {
                score += 40;
                double savings = budget - rent;
                if (savings >= 1000) {
                    score += 5; // bonus for great value
                }
            } else if (rent <= budget * 1.15) {
                score += 15; // slightly over budget
            }

            // 2. Sharing Preference Score (Up to 30 pts)
            if (targetCapacity != null) {
                if (room.getCapacity() == targetCapacity) {
                    score += 30;
                } else if (Math.abs(room.getCapacity() - targetCapacity) == 1) {
                    score += 10;
                }
            } else {
                score += 20; // user has no sharing preference
            }

            // 3. Floor Preference (Up to 15 pts)
            if (floorPref != null) {
                if (room.getFloor() == floorPref) {
                    score += 15;
                }
            } else {
                score += 10;
            }

            // 4. Facilities Score (Up to 15 pts)
            String roomFacilities = (room.getFacilities() != null ? room.getFacilities().toLowerCase() : "");
            int facilitiesMatched = 0;
            for (String fac : facilitiesPref) {
                if (roomFacilities.contains(fac.toLowerCase().trim())) {
                    facilitiesMatched++;
                }
            }
            if (!facilitiesPref.isEmpty()) {
                score += Math.min(15, (facilitiesMatched * 15) / facilitiesPref.size());
            } else {
                score += 10;
            }

            // Cap at 100
            score = Math.min(100, Math.max(30, score));

            String reason = String.format("Room %s (%s) is priced at ₹%.0f/month on Floor %d, offering %s.",
                    room.getRoomNumber(), room.getRoomType(), rent, room.getFloor(), room.getFacilities());

            rankedList.add(new RoomRecommendationResponse.RankedRoom(room, score, reason));
        }

        // Sort by match score descending, then price ascending
        rankedList.sort((a, b) -> {
            int scoreComp = Integer.compare(b.getMatchScore(), a.getMatchScore());
            if (scoreComp != 0) return scoreComp;
            return Double.compare(a.getRoom().getRent(), b.getRoom().getRent());
        });

        // AI narrative explanation if Gemini is configured
        boolean aiPowered = false;
        String summary = String.format("Found %d matching available rooms ranked by your preferences.", rankedList.size());

        if (aiService.isConfigured() && !rankedList.isEmpty()) {
            try {
                Room topRoom = rankedList.get(0).getRoom();
                String prompt = String.format(
                        "A tenant is looking for a PG room with budget ₹%.0f, sharing '%s', floor '%s'. "
                                + "Our top recommendation is Room %s on Floor %d (%s) with rent ₹%.0f and facilities: '%s'. "
                                + "In 2 concise, welcoming sentences, explain why this room is their best choice.",
                        budget, sharingPref, (floorPref != null ? floorPref : "Any"),
                        topRoom.getRoomNumber(), topRoom.getFloor(), topRoom.getRoomType(), topRoom.getRent(), topRoom.getFacilities()
                );
                String aiText = aiService.generateText(prompt, "You are a professional PG accommodation advisor.");
                if (aiText != null && !aiText.isBlank()) {
                    summary = aiText.trim();
                    rankedList.get(0).setReason(aiText.trim());
                    aiPowered = true;
                }
            } catch (Exception ignored) {
            }
        }

        return new RoomRecommendationResponse(rankedList, summary, aiPowered);
    }

    public RoomSearchResponse searchRoomsNaturalLanguage(RoomSearchRequest request) {
        String query = request.getQuery() != null ? request.getQuery().trim() : "";
        Map<String, Object> criteria = extractSearchCriteria(query);

        List<Room> availableRooms = roomRepository.findByStatus("AVAILABLE");

        Double maxBudget = (Double) criteria.get("maxBudget");
        Integer capacity = (Integer) criteria.get("capacity");
        Integer floor = (Integer) criteria.get("floor");
        @SuppressWarnings("unchecked")
        List<String> facilities = (List<String>) criteria.getOrDefault("facilities", Collections.emptyList());

        // Deterministic filtering using verified MySQL records
        List<Room> matched = availableRooms.stream().filter(room -> {
            if (maxBudget != null && room.getRent() > maxBudget) {
                return false;
            }
            if (capacity != null && room.getCapacity() != capacity) {
                return false;
            }
            if (floor != null && room.getFloor() != floor) {
                return false;
            }
            if (!facilities.isEmpty()) {
                String roomFac = room.getFacilities() != null ? room.getFacilities().toLowerCase() : "";
                for (String req : facilities) {
                    if (!roomFac.contains(req.toLowerCase())) {
                        return false;
                    }
                }
            }
            return true;
        }).sorted(Comparator.comparingDouble(Room::getRent)).collect(Collectors.toList());

        // If strict match gave 0 results, fall back to soft matching (budget or capacity)
        if (matched.isEmpty() && !availableRooms.isEmpty()) {
            matched = availableRooms.stream().filter(room -> {
                if (maxBudget != null && room.getRent() <= maxBudget) return true;
                if (capacity != null && room.getCapacity() == capacity) return true;
                return false;
            }).sorted(Comparator.comparingDouble(Room::getRent)).collect(Collectors.toList());
        }

        boolean aiPowered = criteria.get("aiPowered") == Boolean.TRUE;
        String explanation;
        if (matched.isEmpty()) {
            explanation = "No available rooms matched your exact criteria. Showing all active options or consider adjusting your budget.";
            matched = availableRooms;
        } else {
            explanation = String.format("Found %d verified available room(s) matching '%s'.", matched.size(), query);
        }

        return new RoomSearchResponse(query, criteria, matched, explanation, aiPowered);
    }

    private Map<String, Object> extractSearchCriteria(String query) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("aiPowered", false);
        List<String> facilities = new ArrayList<>();

        // Try AI extraction first if available
        if (aiService.isConfigured() && query.length() > 5) {
            try {
                String prompt = "Extract search parameters from this PG room search query as a strict JSON object with keys: "
                        + "\"maxBudget\" (number or null), \"capacity\" (integer: 1 for single, 2 for double, 3 for triple, 4 for 4-sharing, or null), "
                        + "\"floor\" (integer or null), \"facilities\" (array of string keywords like [\"Wi-Fi\", \"AC\", \"Geyser\"] or []).\n"
                        + "Query: \"" + query + "\"\n"
                        + "Reply with ONLY valid JSON and no markdown formatting.";

                String aiResult = aiService.generateText(prompt, "You are a precise data extraction engine.");
                if (aiResult != null && !aiResult.isBlank()) {
                    String cleanJson = aiResult.replaceAll("```json", "").replaceAll("```", "").trim();
                    JsonNode node = objectMapper.readTree(cleanJson);
                    if (node.has("maxBudget") && !node.get("maxBudget").isNull()) {
                        criteria.put("maxBudget", node.get("maxBudget").asDouble());
                    }
                    if (node.has("capacity") && !node.get("capacity").isNull()) {
                        criteria.put("capacity", node.get("capacity").asInt());
                    }
                    if (node.has("floor") && !node.get("floor").isNull()) {
                        criteria.put("floor", node.get("floor").asInt());
                    }
                    if (node.has("facilities") && node.get("facilities").isArray()) {
                        for (JsonNode f : node.get("facilities")) {
                            facilities.add(f.asText());
                        }
                        criteria.put("facilities", facilities);
                    }
                    criteria.put("aiPowered", true);
                    return criteria;
                }
            } catch (Exception ignored) {
            }
        }

        // Deterministic NLP Rule-based extraction fallback
        String lower = query.toLowerCase();

        // 1. Budget extraction (e.g. "under 7000", "below ₹8,000", "< 6000", "budget 7500")
        Pattern budgetPattern = Pattern.compile("(under|below|less than|budget|within|max|₹|rs\\.?)\\s*([0-9]{3,6})");
        Matcher budgetMatcher = budgetPattern.matcher(lower);
        if (budgetMatcher.find()) {
            criteria.put("maxBudget", Double.parseDouble(budgetMatcher.group(2)));
        } else {
            Pattern numPattern = Pattern.compile("([0-9]{4,5})");
            Matcher numMatcher = numPattern.matcher(lower);
            if (numMatcher.find()) {
                criteria.put("maxBudget", Double.parseDouble(numMatcher.group(1)));
            }
        }

        // 2. Sharing / Capacity extraction
        if (lower.contains("single") || lower.contains("1 sharing") || lower.contains("private") || lower.contains("1 person")) {
            criteria.put("capacity", 1);
        } else if (lower.contains("double") || lower.contains("2 sharing") || lower.contains("twin") || lower.contains("2 person")) {
            criteria.put("capacity", 2);
        } else if (lower.contains("triple") || lower.contains("3 sharing") || lower.contains("3 person")) {
            criteria.put("capacity", 3);
        } else if (lower.contains("4 sharing") || lower.contains("four sharing") || lower.contains("4 person")) {
            criteria.put("capacity", 4);
        }

        // 3. Floor extraction
        Pattern floorPattern = Pattern.compile("([1-9])(st|nd|rd|th)?\\s*floor");
        Matcher floorMatcher = floorPattern.matcher(lower);
        if (floorMatcher.find()) {
            criteria.put("floor", Integer.parseInt(floorMatcher.group(1)));
        }

        // 4. Facilities extraction
        if (lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("internet")) {
            facilities.add("Wi-Fi");
        }
        if (lower.contains("ac") || lower.contains("air condition")) {
            facilities.add("AC");
        }
        if (lower.contains("geyser") || lower.contains("hot water")) {
            facilities.add("Geyser");
        }
        if (lower.contains("bathroom") || lower.contains("attached")) {
            facilities.add("Attached Bathroom");
        }
        if (lower.contains("laundry") || lower.contains("washing")) {
            facilities.add("Laundry");
        }
        if (lower.contains("study") || lower.contains("desk")) {
            facilities.add("Study Desk");
        }
        criteria.put("facilities", facilities);

        return criteria;
    }

    private Integer getCapacityFromSharingPreference(String sharing) {
        if (sharing.contains("single") || sharing.contains("1")) return 1;
        if (sharing.contains("double") || sharing.contains("2")) return 2;
        if (sharing.contains("triple") || sharing.contains("3")) return 3;
        if (sharing.contains("4") || sharing.contains("four")) return 4;
        return null;
    }
}
