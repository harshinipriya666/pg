package com.pgmanagement.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgmanagement.ai.dto.ComplaintAssistRequest;
import com.pgmanagement.ai.dto.ComplaintAssistResponse;
import com.pgmanagement.ai.service.AIComplaintService;
import com.pgmanagement.ai.service.AIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class AIComplaintServiceTest {

    private AIComplaintService complaintService;
    private AIService aiService;

    @BeforeEach
    void setUp() {
        aiService = Mockito.mock(AIService.class);
        Mockito.when(aiService.isConfigured()).thenReturn(false); // test deterministic engine
        complaintService = new AIComplaintService(aiService, new ObjectMapper());
    }

    @Test
    void testPlumbingLeakClassification() {
        ComplaintAssistRequest req = new ComplaintAssistRequest();
        req.setSubject("Water leak in bathroom");
        req.setDescription("The main pipe under the sink is leaking water continuously.");

        ComplaintAssistResponse res = complaintService.assistComplaint(req);
        assertEquals("PLUMBING", res.getSuggestedCategory());
        assertEquals("HIGH", res.getSuggestedPriority());
        assertNotNull(res.getPriorityReason());
    }

    @Test
    void testElectricitySparkClassification() {
        ComplaintAssistRequest req = new ComplaintAssistRequest();
        req.setSubject("Spark in switchboard");
        req.setDescription("Switchboard has sparks when turning on light.");

        ComplaintAssistResponse res = complaintService.assistComplaint(req);
        assertEquals("ELECTRICITY", res.getSuggestedCategory());
        assertEquals("HIGH", res.getSuggestedPriority());
    }

    @Test
    void testCleaningClassification() {
        ComplaintAssistRequest req = new ComplaintAssistRequest();
        req.setSubject("Dusty corridor");
        req.setDescription("Corridor floor needs sweeping and garbage removal.");

        ComplaintAssistResponse res = complaintService.assistComplaint(req);
        assertEquals("CLEANING", res.getSuggestedCategory());
        assertEquals("MEDIUM", res.getSuggestedPriority());
    }

    @Test
    void testEmptyDescriptionGracefulHandling() {
        ComplaintAssistRequest req = new ComplaintAssistRequest();
        ComplaintAssistResponse res = complaintService.assistComplaint(req);
        assertNotNull(res);
        assertEquals("OTHER", res.getSuggestedCategory());
    }
}
