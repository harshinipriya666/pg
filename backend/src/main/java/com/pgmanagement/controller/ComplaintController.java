package com.pgmanagement.controller;

import com.pgmanagement.entity.Complaint;
import com.pgmanagement.service.ComplaintService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ComplaintController {

    private final ComplaintService service;

    public ComplaintController(ComplaintService service) {
        this.service = service;
    }

    @GetMapping
    public List<Complaint> getAll() {
        return service.getAll();
    }

    @GetMapping("/student/{studentId}")
    public List<Complaint> getByStudent(
            @PathVariable Long studentId) {

        return service.getByStudent(studentId);
    }

    @PostMapping("/student/{studentId}")
    public Complaint create(
            @PathVariable Long studentId,
            @RequestBody Complaint complaint) {

        return service.create(studentId, complaint);
    }

    @PutMapping("/{id}/status")
    public Complaint updateStatus(
            @PathVariable Long id,
            @RequestParam String value) {

        return service.updateStatus(id, value);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {

        service.delete(id);

        return "Complaint deleted successfully";
    }
}