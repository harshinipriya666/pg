package com.pgmanagement.ai.controller;

import com.pgmanagement.ai.config.AIConfig;
import com.pgmanagement.ai.dto.*;
import com.pgmanagement.ai.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AIController {

    private final AIChatService aiChatService;
    private final AIRoomService aiRoomService;
    private final AIComplaintService aiComplaintService;
    private final AIAdminInsightsService aiAdminInsightsService;
    private final AIConfig aiConfig;

    public AIController(
            AIChatService aiChatService,
            AIRoomService aiRoomService,
            AIComplaintService aiComplaintService,
            AIAdminInsightsService aiAdminInsightsService,
            AIConfig aiConfig) {
        this.aiChatService = aiChatService;
        this.aiRoomService = aiRoomService;
        this.aiComplaintService = aiComplaintService;
        this.aiAdminInsightsService = aiAdminInsightsService;
        this.aiConfig = aiConfig;
    }

    /**
     * AI Health and Configuration Status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAIStatus() {
        return ResponseEntity.ok(Map.of(
                "configured", aiConfig.isConfigured(),
                "provider", "Google Gemini",
                "model", aiConfig.getModel(),
                "fallbackModeAvailable", true
        ));
    }

    /**
     * Feature 1: Grounded AI Chatbot
     * POST /api/ai/chat
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String headerEmail,
            @RequestHeader(value = "X-User-Role", required = false) String headerRole,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {

        if (request.getUserEmail() == null && headerEmail != null) {
            request.setUserEmail(headerEmail);
        }
        if (request.getUserRole() == null && headerRole != null) {
            request.setUserRole(headerRole);
        }
        if (request.getUserId() == null && headerUserId != null) {
            request.setUserId(headerUserId);
        }

        ChatResponse response = aiChatService.processChat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Feature 2: AI-Based Room Recommendations
     * POST /api/ai/room-recommendations
     */
    @PostMapping("/room-recommendations")
    public ResponseEntity<RoomRecommendationResponse> getRoomRecommendations(
            @RequestBody RoomRecommendationRequest request) {

        RoomRecommendationResponse response = aiRoomService.recommendRooms(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Feature 5: Natural-Language Room Search
     * POST /api/ai/room-search
     */
    @PostMapping("/room-search")
    public ResponseEntity<RoomSearchResponse> searchRooms(
            @RequestBody RoomSearchRequest request) {

        RoomSearchResponse response = aiRoomService.searchRoomsNaturalLanguage(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Feature 3: AI Complaint Assistant
     * POST /api/ai/complaint-assistance
     */
    @PostMapping("/complaint-assistance")
    public ResponseEntity<ComplaintAssistResponse> assistComplaint(
            @RequestBody ComplaintAssistRequest request) {

        ComplaintAssistResponse response = aiComplaintService.assistComplaint(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Feature 4: AI-Powered Admin Insights
     * GET /api/ai/admin-insights
     */
    @GetMapping("/admin-insights")
    public ResponseEntity<?> getAdminInsights(
            @RequestHeader(value = "X-User-Role", required = false) String headerRole,
            @RequestParam(value = "role", required = false) String paramRole) {

        String userRole = headerRole != null ? headerRole : paramRole;

        // Verify Administrator permissions
        if (userRole != null && !"ADMIN".equalsIgnoreCase(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access denied. Admin insights are restricted to administrators."));
        }

        AdminInsightsResponse response = aiAdminInsightsService.generateInsights();
        return ResponseEntity.ok(response);
    }
}
