package com.pgmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Student student;

    private int rating;
    private int accommodationRating;
    private int foodRating;
    private int cleanlinessRating;
    private int facilitiesRating;

    @Column(length = 2000)
    private String comment;

    private LocalDateTime createdDate = LocalDateTime.now();

    public Review() {
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getAccommodationRating() {
        return accommodationRating;
    }

    public void setAccommodationRating(int accommodationRating) {
        this.accommodationRating = accommodationRating;
    }

    public int getFoodRating() {
        return foodRating;
    }

    public void setFoodRating(int foodRating) {
        this.foodRating = foodRating;
    }

    public int getCleanlinessRating() {
        return cleanlinessRating;
    }

    public void setCleanlinessRating(int cleanlinessRating) {
        this.cleanlinessRating = cleanlinessRating;
    }

    public int getFacilitiesRating() {
        return facilitiesRating;
    }

    public void setFacilitiesRating(int facilitiesRating) {
        this.facilitiesRating = facilitiesRating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}