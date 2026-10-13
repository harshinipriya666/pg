package com.pgmanagement.controller;

import com.pgmanagement.entity.Student;
import com.pgmanagement.service.StudentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "http://localhost:5173")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping
    public List<Student> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Student getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/search")
    public List<Student> search(@RequestParam String name) {
        return service.search(name);
    }

    @PostMapping
    public Student create(@RequestBody Student student) {
        return service.save(student);
    }

    @PutMapping("/{id}")
    public Student update(
            @PathVariable Long id,
            @RequestBody Student student) {

        return service.update(id, student);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {

        service.delete(id);

        return "Student deleted successfully";
    }
}