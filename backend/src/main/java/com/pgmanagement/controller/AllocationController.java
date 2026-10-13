package com.pgmanagement.controller;

import com.pgmanagement.entity.Allocation;
import com.pgmanagement.service.AllocationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/allocations")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AllocationController {

    private final AllocationService service;

    public AllocationController(AllocationService service) {
        this.service = service;
    }

    @GetMapping
    public List<Allocation> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Allocation getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public Allocation allocate(@RequestBody Map<String, Long> data) {

        Long studentId = data.get("studentId");
        Long roomId = data.get("roomId");

        return service.allocate(studentId, roomId);
    }

    @PutMapping("/{id}/status")
    public Allocation updateStatus(
            @PathVariable Long id,
            @RequestParam String value) {

        return service.updateStatus(id, value);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {

        service.delete(id);

        return "Allocation deleted successfully";
    }
}