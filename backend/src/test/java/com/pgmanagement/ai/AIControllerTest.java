package com.pgmanagement.ai;

import com.pgmanagement.ai.config.AIConfig;
import com.pgmanagement.ai.controller.AIController;
import com.pgmanagement.ai.dto.*;
import com.pgmanagement.ai.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AIControllerTest {

    private AIController aiController;
    private AIChatService aiChatService;
    private AIRoomService aiRoomService;
    private AIComplaintService aiComplaintService;
    private AIAdminInsightsService aiAdminInsightsService;
    private AIConfig aiConfig;

    @BeforeEach
    void setUp() {
        aiChatService = Mockito.mock(AIChatService.class);
        aiRoomService = Mockito.mock(AIRoomService.class);
        aiComplaintService = Mockito.mock(AIComplaintService.class);
        aiAdminInsightsService = Mockito.mock(AIAdminInsightsService.class);
        aiConfig = Mockito.mock(AIConfig.class);

        aiController = new AIController(
                aiChatService,
                aiRoomService,
                aiComplaintService,
                aiAdminInsightsService,
                aiConfig
        );
    }

    @Test
    void testChatEndpoint() {
        when(aiChatService.processChat(any())).thenReturn(new ChatResponse("Hello tenant!", false, List.of()));

        ChatRequest req = new ChatRequest();
        req.setMessage("Hi");

        ResponseEntity<ChatResponse> res = aiController.chat(req, "test@test.com", "STUDENT", 1L);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals("Hello tenant!", res.getBody().getReply());
    }

    @Test
    void testAdminInsightsForbiddenForNonAdmin() {
        ResponseEntity<?> res = aiController.getAdminInsights("STUDENT", null);
        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
    }

    @Test
    void testAdminInsightsAllowedForAdmin() {
        when(aiAdminInsightsService.generateInsights()).thenReturn(new AdminInsightsResponse());

        ResponseEntity<?> res = aiController.getAdminInsights("ADMIN", null);
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }
}
