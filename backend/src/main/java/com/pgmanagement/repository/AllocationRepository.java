package com.pgmanagement.repository;

import com.pgmanagement.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllocationRepository extends JpaRepository<Allocation, Long> {

    List<Allocation> findByStudentId(Long studentId);

    List<Allocation> findByRoomId(Long roomId);
}