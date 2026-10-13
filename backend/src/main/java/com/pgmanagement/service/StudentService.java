package com.pgmanagement.service;

import com.pgmanagement.entity.Student;
import com.pgmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    public List<Student> getAll() {
        return repository.findAll();
    }

    public Student getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    public List<Student> search(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }

    public Student save(Student student) {
        return repository.save(student);
    }

    public Student update(Long id, Student data) {

        Student student = getById(id);

        student.setName(data.getName());
        student.setEmail(data.getEmail());
        student.setPhone(data.getPhone());
        student.setAddress(data.getAddress());
        student.setCourse(data.getCourse());
        student.setYear(data.getYear());

        return repository.save(student);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}