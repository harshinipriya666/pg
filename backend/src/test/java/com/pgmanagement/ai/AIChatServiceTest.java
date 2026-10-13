package com.pgmanagement.ai;

import com.pgmanagement.ai.dto.ChatRequest;
import com.pgmanagement.ai.dto.ChatResponse;
import com.pgmanagement.ai.service.AIChatService;
import com.pgmanagement.ai.service.AIService;
import com.pgmanagement.entity.Fee;
import com.pgmanagement.entity.Room;
import com.pgmanagement.entity.Student;
import com.pgmanagement.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AIChatServiceTest {

    private AIChatService chatService;
    private AIService aiService;
    private RoomRepository roomRepository;
    private StudentRepository studentRepository;
    private AllocationRepository allocationRepository;
    private FeeRepository feeRepository;
    private ComplaintRepository complaintRepository;
    private MealScheduleRepository mealScheduleRepository;

    @BeforeEach
    void setUp() {
        aiService = Mockito.mock(AIService.class);
        roomRepository = Mockito.mock(RoomRepository.class);
        studentRepository = Mockito.mock(StudentRepository.class);
        allocationRepository = Mockito.mock(AllocationRepository.class);
        feeRepository = Mockito.mock(FeeRepository.class);
        complaintRepository = Mockito.mock(ComplaintRepository.class);
        mealScheduleRepository = Mockito.mock(MealScheduleRepository.class);

        when(aiService.isConfigured()).thenReturn(false);

        chatService = new AIChatService(
                aiService,
                roomRepository,
                studentRepository,
                allocationRepository,
                feeRepository,
                complaintRepository,
                mealScheduleRepository
        );
    }

    @Test
    void testChatAnswersRoomAvailability() {
        Room r1 = new Room();
        r1.setId(1L);
        r1.setRoomNumber("101");
        r1.setFloor(1);
        r1.setCapacity(2);
        r1.setRent(6000.0);
        r1.setStatus("AVAILABLE");

        when(roomRepository.findAll()).thenReturn(List.of(r1));

        ChatRequest req = new ChatRequest();
        req.setMessage("Which rooms are available right now?");

        ChatResponse res = chatService.processChat(req);
        assertNotNull(res);
        assertNotNull(res.getReply());
        assertTrue(res.getReply().contains("101") || res.getReply().contains("Available"));
    }

    @Test
    void testChatStudentFeePrivacy() {
        Student s = new Student();
        s.setId(10L);
        s.setName("Test Student");
        s.setEmail("student@test.com");

        Fee fee = new Fee();
        fee.setId(100L);
        fee.setAmount(6500.0);
        fee.setDueDate(LocalDate.now().plusDays(5));
        fee.setPaymentStatus("PENDING");

        when(studentRepository.findByEmail("student@test.com")).thenReturn(s);
        when(feeRepository.findByStudentId(10L)).thenReturn(List.of(fee));

        ChatRequest req = new ChatRequest();
        req.setUserEmail("student@test.com");
        req.setMessage("What is my pending fee?");

        ChatResponse res = chatService.processChat(req);
        assertNotNull(res);
        assertTrue(res.getReply().contains("6500") || res.getReply().contains("PENDING"));
    }
}
