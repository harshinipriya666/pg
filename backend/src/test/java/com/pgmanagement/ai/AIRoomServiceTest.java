package com.pgmanagement.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgmanagement.ai.dto.*;
import com.pgmanagement.ai.service.AIRoomService;
import com.pgmanagement.ai.service.AIService;
import com.pgmanagement.entity.Room;
import com.pgmanagement.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AIRoomServiceTest {

    private AIRoomService roomService;
    private RoomRepository roomRepository;
    private AIService aiService;

    @BeforeEach
    void setUp() {
        roomRepository = Mockito.mock(RoomRepository.class);
        aiService = Mockito.mock(AIService.class);
        when(aiService.isConfigured()).thenReturn(false);

        roomService = new AIRoomService(roomRepository, aiService, new ObjectMapper());
    }

    @Test
    void testRoomRecommendationsRanking() {
        Room r1 = new Room();
        r1.setId(1L);
        r1.setRoomNumber("101");
        r1.setFloor(1);
        r1.setCapacity(2);
        r1.setRent(6000.0);
        r1.setStatus("AVAILABLE");
        r1.setFacilities("Wi-Fi, Attached Bathroom");

        Room r2 = new Room();
        r2.setId(2L);
        r2.setRoomNumber("201");
        r2.setFloor(2);
        r2.setCapacity(1);
        r2.setRent(8500.0);
        r2.setStatus("AVAILABLE");
        r2.setFacilities("Wi-Fi, AC, Attached Bathroom");

        when(roomRepository.findByStatus("AVAILABLE")).thenReturn(List.of(r1, r2));

        RoomRecommendationRequest req = new RoomRecommendationRequest();
        req.setMaxBudget(7000.0);
        req.setSharingPreference("Double");

        RoomRecommendationResponse res = roomService.recommendRooms(req);
        assertNotNull(res);
        assertFalse(res.getRecommendations().isEmpty());
        // Room 101 should rank highest because it fits budget (6000 <= 7000) and matches Double capacity (2)
        assertEquals("101", res.getRecommendations().get(0).getRoom().getRoomNumber());
        assertTrue(res.getRecommendations().get(0).getMatchScore() > 60);
    }

    @Test
    void testNaturalLanguageRoomSearchExtraction() {
        Room r1 = new Room();
        r1.setId(1L);
        r1.setRoomNumber("101");
        r1.setFloor(1);
        r1.setCapacity(2);
        r1.setRent(6500.0);
        r1.setStatus("AVAILABLE");
        r1.setFacilities("Wi-Fi, Cupboard");

        when(roomRepository.findByStatus("AVAILABLE")).thenReturn(List.of(r1));

        RoomSearchRequest req = new RoomSearchRequest("Find double sharing room under 7000 with wifi");
        RoomSearchResponse res = roomService.searchRoomsNaturalLanguage(req);

        assertNotNull(res);
        assertFalse(res.getMatchingRooms().isEmpty());
        assertEquals("101", res.getMatchingRooms().get(0).getRoomNumber());
        assertEquals(2, res.getExtractedCriteria().get("capacity"));
        assertEquals(7000.0, res.getExtractedCriteria().get("maxBudget"));
    }
}
