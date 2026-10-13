package com.pgmanagement.ai.dto;

public class ComplaintAssistRequest {

    private String subject;
    private String description;
    private Long studentId;

    public ComplaintAssistRequest() {
    }

    public ComplaintAssistRequest(String subject, String description, Long studentId) {
        this.subject = subject;
        this.description = description;
        this.studentId = studentId;
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

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
}
