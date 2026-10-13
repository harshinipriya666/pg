package com.pgmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Student student;

    private String subject;

    @Column(length = 2000)
    private String description;

    private String status = "PENDING";

    private LocalDateTime createdDate = LocalDateTime.now();

    private String category = "OTHER";

    private String priority = "MEDIUM";

    @Column(length = 1000)
    private String priorityReason;

    @Column(length = 1000)
    private String aiSummary;

    public Complaint() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getCategory() {
        return category != null && !category.isBlank() ? category : "OTHER";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority != null && !priority.isBlank() ? priority : "MEDIUM";
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getPriorityReason() {
        return priorityReason;
    }

    public void setPriorityReason(String priorityReason) {
        this.priorityReason = priorityReason;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public void setAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }
}