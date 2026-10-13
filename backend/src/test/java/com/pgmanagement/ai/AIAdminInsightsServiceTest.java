package com.pgmanagement.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgmanagement.ai.dto.AdminInsightsResponse;
import com.pgmanagement.ai.service.AIAdminInsightsService;
import com.pgmanagement.ai.service.AIService;
import com.pgmanagement.entity.Fee;
import com.pgmanagement.entity.Room;
import com.pgmanagement.entity.Student;
import com.pgmanagement.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AIAdminInsightsServiceTest {

    private AIAdminInsightsService insightsService;
    private StudentRepository studentRepository;
    private RoomRepository roomRepository;
    private FeeRepository feeRepository;
    private ComplaintRepository complaintRepository;
    private ReviewRepository reviewRepository;
    private AIService aiService;

    @BeforeEach
    void setUp() {
        studentRepository = Mockito.mock(StudentRepository.class);
        roomRepository = Mockito.mock(RoomRepository.class);
        feeRepository = Mockito.mock(FeeRepository.class);
        complaintRepository = Mockito.mock(ComplaintRepository.class);
        reviewRepository = Mockito.mock(ReviewRepository.class);
        aiService = Mockito.mock(AIService.class);

        when(aiService.isConfigured()).thenReturn(false);

        insightsService = new AIAdminInsightsService(
                studentRepository,
                roomRepository,
                feeRepository,
                complaintRepository,
                reviewRepository,
                aiService,
                new ObjectMapper()
        );
    }

    @Test
    void testGenerateInsightsDeterministic() {
        when(studentRepository.count()).thenReturn(5L);

        Room r1 = new Room();
        r1.setId(1L);
        r1.setRoomNumber("101");
        r1.setCapacity(2);
        r1.setOccupied(2);
        r1.setStatus("OCCUPIED");
        r1.setRent(6000.0);

        Room r2 = new Room();
        r2.setId(2L);
        r2.setRoomNumber("102");
        r2.setCapacity(2);
        r2.setOccupied(0);
        r2.setStatus("AVAILABLE");
        r2.setRent(6000.0);

        when(roomRepository.findAll()).thenReturn(List.of(r1, r2));

        Fee f1 = new Fee();
        f1.setAmount(6000.0);
        f1.setPaymentStatus("PAID");

        Fee f2 = new Fee();
        f2.setAmount(6000.0);
        f2.setPaymentStatus("PENDING");

        when(feeRepository.findAll()).thenReturn(List.of(f1, f2));

        AdminInsightsResponse res = insightsService.generateInsights();

        assertNotNull(res);
        assertNotNull(res.getMetrics());
        assertEquals(5L, res.getMetrics().getTotalStudents());
        assertEquals(2L, res.getMetrics().getTotalRooms());
        assertEquals(1L, res.getMetrics().getAvailableRooms());
        assertEquals(50.0, res.getMetrics().getOccupancyRate()); // 2 occupied of 4 beds = 50%
        assertEquals(6000.0, res.getMetrics().getTotalRentCollected());
        assertEquals(6000.0, res.getMetrics().getOutstandingRent());

        assertNotNull(res.getExecutiveSummary());
        assertFalse(res.getKeyStrengths().isEmpty());
        assertFalse(res.getCriticalAlerts().isEmpty());
        assertFalse(res.getActionableRecommendations().isEmpty());
    }
}
