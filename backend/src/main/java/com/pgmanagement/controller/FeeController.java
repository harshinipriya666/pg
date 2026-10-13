package com.pgmanagement.controller;

import com.pgmanagement.entity.Fee;
import com.pgmanagement.service.FeeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fees")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class FeeController {

    private final FeeService service;

    public FeeController(FeeService service) {
        this.service = service;
    }

    @GetMapping
    public List<Fee> getAll() {
        return service.getAll();
    }

    @GetMapping("/student/{studentId}")
    public List<Fee> getByStudent(
            @PathVariable Long studentId) {

        return service.getByStudent(studentId);
    }

    @PostMapping("/student/{studentId}")
    public Fee create(
            @PathVariable Long studentId,
            @RequestBody Fee fee) {

        return service.create(studentId, fee);
    }

    @PutMapping("/{id}/status")
    public Fee updateStatus(
            @PathVariable Long id,
            @RequestParam String value) {

        return service.updateStatus(id, value);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {

        service.delete(id);

        return "Fee deleted successfully";
    }
}