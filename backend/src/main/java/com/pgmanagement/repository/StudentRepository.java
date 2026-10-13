package com.pgmanagement.repository;

import com.pgmanagement.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByNameContainingIgnoreCase(String name);

    Student findByEmail(String email);

    Student findByUserId(Long userId);
}