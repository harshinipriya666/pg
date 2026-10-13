package com.pgmanagement.service;

import com.pgmanagement.entity.Review;
import com.pgmanagement.entity.Student;
import com.pgmanagement.repository.ReviewRepository;
import com.pgmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StudentRepository studentRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            StudentRepository studentRepository) {

        this.reviewRepository = reviewRepository;
        this.studentRepository = studentRepository;
    }

    public List<Review> getAll() {
        return reviewRepository.findAll();
    }

    public List<Review> getByStudent(Long studentId) {
        return reviewRepository.findByStudentId(studentId);
    }

    public Review create(Long studentId, Review review) {

        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        review.setStudent(student);

        return reviewRepository.save(review);
    }

    public void delete(Long id) {
        reviewRepository.deleteById(id);
    }
}