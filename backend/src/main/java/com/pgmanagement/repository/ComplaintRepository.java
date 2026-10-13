package com.pgmanagement.repository;

import com.pgmanagement.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByStudentId(Long studentId);

    List<Complaint> findByStatus(String status);

    List<Complaint> findByCategory(String category);
}