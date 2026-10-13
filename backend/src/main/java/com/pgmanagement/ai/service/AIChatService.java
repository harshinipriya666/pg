package com.pgmanagement.ai.service;

import com.pgmanagement.ai.dto.ChatRequest;
import com.pgmanagement.ai.dto.ChatResponse;
import com.pgmanagement.entity.*;
import com.pgmanagement.repository.*;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIChatService {

    private final AIService aiService;
    private final RoomRepository roomRepository;
    private final StudentRepository studentRepository;
    private final AllocationRepository allocationRepository;
    private final FeeRepository feeRepository;
    private final ComplaintRepository complaintRepository;
    private final MealScheduleRepository mealScheduleRepository;

    public AIChatService(
            AIService aiService,
            RoomRepository roomRepository,
            StudentRepository studentRepository,
            AllocationRepository allocationRepository,
            FeeRepository feeRepository,
            ComplaintRepository complaintRepository,
            MealScheduleRepository mealScheduleRepository) {
        this.aiService = aiService;
        this.roomRepository = roomRepository;
        this.studentRepository = studentRepository;
        this.allocationRepository = allocationRepository;
        this.feeRepository = feeRepository;
        this.complaintRepository = complaintRepository;
        this.mealScheduleRepository = mealScheduleRepository;
    }

    public ChatResponse processChat(ChatRequest request) {
        String userMessage = request.getMessage() != null ? request.getMessage().trim() : "";
        if (userMessage.isEmpty()) {
            return new ChatResponse("Please enter a question about PG rooms, amenities, rent, meals, or complaints.", false, getDefaultSuggestions());
        }

        // 1. Gather Real System Data
        List<Room> allRooms = roomRepository.findAll();
        List<Room> availableRooms = allRooms.stream()
                .filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus()))
                .collect(Collectors.toList());

        List<MealSchedule> meals = mealScheduleRepository.findAll();

        // 2. Gather User-Specific Context (Strict Authorization & Privacy)
        Student currentStudent = null;
        List<Allocation> studentAllocations = Collections.emptyList();
        List<Fee> studentFees = Collections.emptyList();
        List<Complaint> studentComplaints = Collections.emptyList();

        if (request.getUserEmail() != null && !request.getUserEmail().isBlank()) {
            currentStudent = studentRepository.findByEmail(request.getUserEmail());
        } else if (request.getUserId() != null) {
            currentStudent = studentRepository.findByUserId(request.getUserId());
        }

        if (currentStudent != null) {
            studentAllocations = allocationRepository.findByStudentId(currentStudent.getId());
            studentFees = feeRepository.findByStudentId(currentStudent.getId());
            studentComplaints = complaintRepository.findByStudentId(currentStudent.getId());
        }

        // 3. Build Grounded Context Text
        StringBuilder context = new StringBuilder();
        context.append("--- VERIFIED PG SYSTEM REAL-TIME DATA ---\n");
        context.append(String.format("Total Rooms: %d, Available Rooms: %d\n", allRooms.size(), availableRooms.size()));

        if (!availableRooms.isEmpty()) {
            context.append("Available Rooms Details:\n");
            for (Room r : availableRooms) {
                context.append(String.format("- Room %s (Floor %d): %s, Capacity %d (Occupied: %d), Rent: ₹%.0f/mo, Amenities: %s\n",
                        r.getRoomNumber(), r.getFloor(), r.getRoomType(), r.getCapacity(), r.getOccupied(), r.getRent(), r.getFacilities()));
            }
        } else {
            context.append("Currently all rooms are fully occupied.\n");
        }

        context.append("\nMeal Timings & Schedules:\n");
        if (meals.isEmpty()) {
            context.append("- Breakfast: 7:30 AM - 9:30 AM, Lunch: 12:30 PM - 2:30 PM, Dinner: 7:30 PM - 9:30 PM\n");
        } else {
            for (MealSchedule m : meals) {
                context.append(String.format("- %s at %s: %s\n", m.getMealType(), m.getMealTime(),
                        m.getNotificationMessage() != null ? m.getNotificationMessage() : "Regular meal provided"));
            }
        }

        context.append("\nPG Policies & Amenities:\n");
        context.append("- Amenities included: High-speed Wi-Fi (100 Mbps), Daily Housekeeping, RO Water Purifier, Washing Machine / Laundry access, 24/7 Power Backup, CCTV Security, Study Desks.\n");
        context.append("- Gate Closing Time: 10:00 PM on weekdays, 10:30 PM on weekends.\n");
        context.append("- Visitors: Allowed in lounge area from 10:00 AM to 7:00 PM. No overnight outside guests without warden permission.\n");
        context.append("- Notice Period: 30 days notice required before vacating.\n");

        if (currentStudent != null) {
            context.append(String.format("\nAuthenticated Tenant Info for %s (Student ID: %d, Email: %s):\n",
                    currentStudent.getName(), currentStudent.getId(), currentStudent.getEmail()));

            if (studentAllocations.isEmpty()) {
                context.append("- Room Allocation: No active room allocation currently.\n");
            } else {
                for (Allocation a : studentAllocations) {
                    context.append(String.format("- Allocated Room: Room %s (Status: %s, Allocation Date: %s)\n",
                            a.getRoom() != null ? a.getRoom().getRoomNumber() : "N/A", a.getStatus(), a.getAllocationDate()));
                }
            }

            if (studentFees.isEmpty()) {
                context.append("- Fees: No fee records found.\n");
            } else {
                context.append("- Fee History & Dues:\n");
                for (Fee f : studentFees) {
                    context.append(String.format("  * Fee ID #%d: ₹%.0f (Due: %s, Status: %s)\n",
                            f.getId(), f.getAmount(), f.getDueDate(), f.getPaymentStatus()));
                }
            }

            if (studentComplaints.isEmpty()) {
                context.append("- Complaints: No submitted complaints.\n");
            } else {
                context.append("- Complaints:\n");
                for (Complaint c : studentComplaints) {
                    context.append(String.format("  * Complaint #%d: '%s' (Status: %s, Category: %s, Priority: %s)\n",
                            c.getId(), c.getSubject(), c.getStatus(), c.getCategory(), c.getPriority()));
                }
            }
        } else {
            context.append("\nUser is browsing as an unauthenticated guest or admin.\n");
        }

        // 4. Try Gemini AI
        String systemInstruction = "You are the AI Assistant for the Paying Guest (PG) Management System. "
                + "Answer the tenant or visitor's questions accurately using ONLY the verified PG facts provided. "
                + "Be polite, helpful, concise, and structured. "
                + "Never invent prices, room availability, or tenant payment statuses. "
                + "If asked about tenant fees, room booking, or complaints, use only the authenticated tenant data provided in context. "
                + "Format responses with friendly markdown bullet points and bold text where appropriate.";

        String prompt = "Context:\n" + context.toString() + "\n\nUser Question:\n" + userMessage;

        String aiReply = aiService.generateText(prompt, systemInstruction);

        if (aiReply != null && !aiReply.isBlank()) {
            return new ChatResponse(aiReply, true, generateFollowUpSuggestions(userMessage, currentStudent != null));
        }

        // 5. Intelligent Grounded Fallback Engine
        String fallbackReply = generateDeterministicReply(userMessage, availableRooms, allRooms, meals, currentStudent, studentAllocations, studentFees, studentComplaints);
        return new ChatResponse(fallbackReply, false, generateFollowUpSuggestions(userMessage, currentStudent != null));
    }

    private String generateDeterministicReply(
            String message,
            List<Room> availableRooms,
            List<Room> allRooms,
            List<MealSchedule> meals,
            Student currentStudent,
            List<Allocation> studentAllocations,
            List<Fee> studentFees,
            List<Complaint> studentComplaints) {

        String lower = message.toLowerCase();

        // 1. Room Availability & Types
        if (lower.contains("available") || lower.contains("room") || lower.contains("vacancy") || lower.contains("sharing")) {
            if (availableRooms.isEmpty()) {
                return "Currently, all rooms are fully occupied (" + allRooms.size() + " total rooms). Please check back soon or contact management to be added to the waitlist.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("🏨 **Available Rooms (").append(availableRooms.size()).append(" Rooms Ready)**:\n\n");
            for (Room r : availableRooms) {
                sb.append(String.format("• **Room %s** (Floor %d): %s | Rent: **₹%.0f/month** | Amenities: %s\n",
                        r.getRoomNumber(), r.getFloor(), r.getRoomType(), r.getRent(), r.getFacilities()));
            }
            sb.append("\n👉 You can explore filtered choices on our **Rooms** page or submit an allocation request!");
            return sb.toString();
        }

        // 2. Rent & Pricing
        if (lower.contains("rent") || lower.contains("cost") || lower.contains("price") || lower.contains("fee") || lower.contains("budget")) {
            if (currentStudent != null && (lower.contains("my") || lower.contains("due") || lower.contains("pending"))) {
                if (studentFees.isEmpty()) {
                    return "💰 You currently have no fee records on file. Contact the admin if you require a fee schedule.";
                }
                StringBuilder sb = new StringBuilder("💰 **Your Fee & Payment Status**:\n\n");
                double pendingTotal = 0;
                for (Fee f : studentFees) {
                    sb.append(String.format("• Fee #%d: **₹%.0f** due on **%s** — Status: **%s**\n",
                            f.getId(), f.getAmount(), f.getDueDate(), f.getPaymentStatus()));
                    if ("PENDING".equalsIgnoreCase(f.getPaymentStatus())) {
                        pendingTotal += f.getAmount();
                    }
                }
                if (pendingTotal > 0) {
                    sb.append(String.format("\n⚠️ **Total Outstanding:** ₹%.0f. Please clear your dues at the office.", pendingTotal));
                } else {
                    sb.append("\n✅ All your fees are currently up to date!");
                }
                return sb.toString();
            }

            return "💵 **PG Rent Structure**:\n\n"
                    + "• **Single Sharing:** ₹8,500/month (Private room, attached bathroom)\n"
                    + "• **Double Sharing:** ₹6,500/month (Twin sharing, attached bathroom)\n"
                    + "• **Triple Sharing:** ₹5,500/month (3 sharing, study desks)\n"
                    + "• **4-Sharing:** ₹4,500/month (Budget friendly)\n\n"
                    + "All rent includes high-speed Wi-Fi, electricity allowance, daily cleaning, and water supply!";
        }

        // 3. Food & Meals
        if (lower.contains("food") || lower.contains("meal") || lower.contains("breakfast") || lower.contains("lunch") || lower.contains("dinner") || lower.contains("menu")) {
            StringBuilder sb = new StringBuilder("🍽️ **PG Meal Timings & Food Services**:\n\n");
            if (meals.isEmpty()) {
                sb.append("• **Breakfast:** 7:30 AM – 9:30 AM (Healthy South & North Indian options, Tea/Coffee)\n");
                sb.append("• **Lunch:** 12:30 PM – 2:30 PM (Rice, Dal, Curries, Curd, Roti)\n");
                sb.append("• **Dinner:** 7:30 PM – 9:30 PM (Nutritious dinner with dessert on weekends)\n");
            } else {
                for (MealSchedule m : meals) {
                    sb.append(String.format("• **%s** (%s): %s\n", m.getMealType(), m.getMealTime(),
                            m.getNotificationMessage() != null ? m.getNotificationMessage() : "Wholesome food provided"));
                }
            }
            sb.append("\nOur kitchen uses fresh, hygienic ingredients with clean drinking RO water.");
            return sb.toString();
        }

        // 4. Wi-Fi & Amenities
        if (lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("internet") || lower.contains("amenities") || lower.contains("facilities") || lower.contains("laundry")) {
            return "✨ **Included Amenities & Facilities**:\n\n"
                    + "• 📶 **High-Speed Wi-Fi:** 100 Mbps fiber internet with coverage across all floors.\n"
                    + "• 🧺 **Laundry:** Washing machines available on the terrace & 1st floor.\n"
                    + "• 🧹 **Housekeeping:** Daily room cleaning and common area sanitization.\n"
                    + "• ⚡ **Power Backup:** 24/7 generator backup for lights, fans, and routers.\n"
                    + "• 🔒 **Security:** CCTV surveillance in corridors & biometric entry.\n"
                    + "• 💧 **Water:** 24/7 hot water via geysers & UV/RO drinking water.";
        }

        // 5. Booking & Allocations
        if (lower.contains("booking") || lower.contains("book") || lower.contains("allocate") || lower.contains("allocation")) {
            if (currentStudent != null && (lower.contains("my") || lower.contains("status"))) {
                if (studentAllocations.isEmpty()) {
                    return "You do not have an active room allocation yet. You can browse available rooms and speak to the administrator to allocate one!";
                }
                Allocation a = studentAllocations.get(0);
                return String.format("🏠 **Your Current Room**: Room **%s** (Allocated on: %s, Status: **%s**).",
                        a.getRoom() != null ? a.getRoom().getRoomNumber() : "N/A", a.getAllocationDate(), a.getStatus());
            }
            return "📝 **How to Book a PG Room**:\n\n"
                    + "1. Browse available rooms under the **Rooms** section.\n"
                    + "2. Check your preferred budget and sharing type.\n"
                    + "3. Provide your student details to the administrator, or submit an allocation request.\n"
                    + "4. Upon verification and initial deposit, your room is activated instantly!";
        }

        // 6. Complaints & Issues
        if (lower.contains("complaint") || lower.contains("issue") || lower.contains("problem") || lower.contains("broken") || lower.contains("leak") || lower.contains("repair")) {
            if (currentStudent != null && (lower.contains("my") || lower.contains("status"))) {
                if (studentComplaints.isEmpty()) {
                    return "You have not submitted any complaints yet. If you have an issue, go to the **Complaints** page to submit one!";
                }
                StringBuilder sb = new StringBuilder("🛠️ **Your Complaints Status**:\n\n");
                for (Complaint c : studentComplaints) {
                    sb.append(String.format("• Complaint #%d: **%s** — Status: **%s** (Category: %s, Priority: %s)\n",
                            c.getId(), c.getSubject(), c.getStatus(), c.getCategory(), c.getPriority()));
                }
                return sb.toString();
            }
            return "🛠️ **Complaints Assistance**:\n\n"
                    + "You can submit maintenance, cleaning, electrical, or plumbing complaints on the **Complaints** page.\n"
                    + "Our **AI Complaint Assistant** will automatically categorize your issue, assign a priority level, and notify the administrator for rapid resolution!";
        }

        // 7. PG Rules & Timings
        if (lower.contains("rule") || lower.contains("timing") || lower.contains("gate") || lower.contains("guest") || lower.contains("policy")) {
            return "📜 **PG Rules & Policies**:\n\n"
                    + "• **Gate Timings:** Main gate closes at 10:00 PM (10:30 PM on Saturdays).\n"
                    + "• **Visitors:** Allowed in the common lounge between 10:00 AM and 7:00 PM.\n"
                    + "• **Quiet Hours:** 10:30 PM to 6:00 AM to respect studying roommates.\n"
                    + "• **Smoking & Alcohol:** Strictly prohibited anywhere on the premises.\n"
                    + "• **Notice Period:** 30 days written notice before vacating the accommodation.";
        }

        // General Welcome & Help
        return "👋 Hello! I am your **PG Management AI Assistant**.\n\n"
                + "I can help you with:\n"
                + "• 🛏️ **Room Availability & Pricing** (Single, Double, Triple sharing)\n"
                + "• 🍽️ **Meal Schedule & Food Timings**\n"
                + "• 📶 **Wi-Fi, Laundry & PG Amenities**\n"
                + "• 💰 **Your Rent Status & Due Dates**\n"
                + "• 🛠️ **Filing & Tracking Maintenance Complaints**\n"
                + "• 📜 **PG Rules & Guidelines**\n\n"
                + "What would you like to know today?";
    }

    private List<String> getDefaultSuggestions() {
        return List.of(
                "Which rooms are available?",
                "What is the monthly rent?",
                "Today's meal timings",
                "What amenities are included?"
        );
    }

    private List<String> generateFollowUpSuggestions(String userMessage, boolean isAuthenticated) {
        String lower = userMessage.toLowerCase();
        if (lower.contains("room") || lower.contains("available") || lower.contains("sharing")) {
            return List.of("What is the rent for double sharing?", "What facilities are in the rooms?", "What are the meal timings?");
        }
        if (lower.contains("rent") || lower.contains("fee") || lower.contains("price")) {
            return isAuthenticated
                    ? List.of("Check my pending rent dues", "Which rooms fit under ₹7,000?", "What amenities are included?")
                    : List.of("Which rooms are available?", "What amenities are included?", "How do I book a room?");
        }
        if (lower.contains("food") || lower.contains("meal")) {
            return List.of("What amenities are included?", "What are the PG rules?", "Show available rooms");
        }
        if (lower.contains("complaint") || lower.contains("repair")) {
            return List.of("How do I submit a complaint?", "What are the gate timings?", "Check room availability");
        }
        return getDefaultSuggestions();
    }
}
