package com.pgmanagement.service;

import com.pgmanagement.ai.dto.ComplaintAssistRequest;
import com.pgmanagement.ai.dto.ComplaintAssistResponse;
import com.pgmanagement.ai.service.AIComplaintService;
import com.pgmanagement.entity.Complaint;
import com.pgmanagement.entity.Student;
import com.pgmanagement.repository.ComplaintRepository;
import com.pgmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final StudentRepository studentRepository;
    private final AIComplaintService aiComplaintService;

    public ComplaintService(
            ComplaintRepository complaintRepository,
            StudentRepository studentRepository,
            AIComplaintService aiComplaintService) {

        this.complaintRepository = complaintRepository;
        this.studentRepository = studentRepository;
        this.aiComplaintService = aiComplaintService;
    }

    public List<Complaint> getAll() {
        return complaintRepository.findAll();
    }

    public List<Complaint> getByStudent(Long studentId) {
        return complaintRepository.findByStudentId(studentId);
    }

    public Complaint create(Long studentId, Complaint complaint) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        complaint.setStudent(student);

        // Automatically assign AI category, priority and summary if not already provided
        if (complaint.getCategory() == null || "OTHER".equalsIgnoreCase(complaint.getCategory())
                || complaint.getPriorityReason() == null || complaint.getPriorityReason().isBlank()) {
            try {
                ComplaintAssistRequest assistRequest = new ComplaintAssistRequest();
                assistRequest.setSubject(complaint.getSubject());
                assistRequest.setDescription(complaint.getDescription());
                assistRequest.setStudentId(studentId);

                ComplaintAssistResponse assistResponse = aiComplaintService.assistComplaint(assistRequest);
                if (assistResponse != null) {
                    if (complaint.getCategory() == null || "OTHER".equalsIgnoreCase(complaint.getCategory())) {
                        complaint.setCategory(assistResponse.getSuggestedCategory());
                    }
                    if (complaint.getPriority() == null || "MEDIUM".equalsIgnoreCase(complaint.getPriority())) {
                        complaint.setPriority(assistResponse.getSuggestedPriority());
                    }
                    if (complaint.getPriorityReason() == null || complaint.getPriorityReason().isBlank()) {
                        complaint.setPriorityReason(assistResponse.getPriorityReason());
                    }
                    if (complaint.getAiSummary() == null || complaint.getAiSummary().isBlank()) {
                        complaint.setAiSummary(assistResponse.getSummary());
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return complaintRepository.save(complaint);
    }

    public Complaint updateStatus(Long id, String status) {

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        complaint.setStatus(status);

        return complaintRepository.save(complaint);
    }

    public void delete(Long id) {
        complaintRepository.deleteById(id);
    }
}