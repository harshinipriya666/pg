package com.pgmanagement.controller;

import com.pgmanagement.entity.Review;
import com.pgmanagement.service.ReviewService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReviewController {

    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @GetMapping
    public List<Review> getAll() {
        return service.getAll();
    }

    @GetMapping("/student/{studentId}")
    public List<Review> getByStudent(
            @PathVariable Long studentId) {

        return service.getByStudent(studentId);
    }

    @PostMapping("/student/{studentId}")
    public Review create(
            @PathVariable Long studentId,
            @RequestBody Review review) {

        return service.create(studentId, review);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {

        service.delete(id);

        return "Review deleted successfully";
    }
}