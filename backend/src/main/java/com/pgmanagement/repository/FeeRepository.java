package com.pgmanagement.repository;

import com.pgmanagement.entity.Fee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeeRepository extends JpaRepository<Fee, Long> {

    List<Fee> findByStudentId(Long studentId);

    List<Fee> findByPaymentStatus(String paymentStatus);
}