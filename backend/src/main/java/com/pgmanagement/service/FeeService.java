package com.pgmanagement.service;

import com.pgmanagement.entity.Fee;
import com.pgmanagement.entity.Student;
import com.pgmanagement.repository.FeeRepository;
import com.pgmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeeService {

    private final FeeRepository feeRepository;
    private final StudentRepository studentRepository;

    public FeeService(
            FeeRepository feeRepository,
            StudentRepository studentRepository) {

        this.feeRepository = feeRepository;
        this.studentRepository = studentRepository;
    }

    public List<Fee> getAll() {
        return feeRepository.findAll();
    }

    public List<Fee> getByStudent(Long studentId) {
        return feeRepository.findByStudentId(studentId);
    }

    public Fee create(Long studentId, Fee fee) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        fee.setStudent(student);

        return feeRepository.save(fee);
    }

    public Fee updateStatus(Long id, String status) {

        Fee fee = feeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fee not found"));

        fee.setPaymentStatus(status);

        return feeRepository.save(fee);
    }

    public void delete(Long id) {
        feeRepository.deleteById(id);
    }
}