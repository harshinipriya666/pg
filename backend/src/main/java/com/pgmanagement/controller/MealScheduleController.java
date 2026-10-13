package com.pgmanagement.controller;

import com.pgmanagement.entity.MealSchedule;
import com.pgmanagement.service.MealScheduleService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/meals")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MealScheduleController {

    private final MealScheduleService service;

    public MealScheduleController(MealScheduleService service) {
        this.service = service;
    }

    @GetMapping
    public List<MealSchedule> getAll() {
        return service.getAll();
    }

    @GetMapping("/date/{date}")
    public List<MealSchedule> getByDate(
            @PathVariable String date) {

        return service.getByDate(LocalDate.parse(date));
    }

    @PostMapping
    public MealSchedule create(
            @RequestBody MealSchedule meal) {

        return service.save(meal);
    }

    @PutMapping("/{id}")
    public MealSchedule update(
            @PathVariable Long id,
            @RequestBody MealSchedule meal) {

        return service.update(id, meal);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {

        service.delete(id);

        return "Meal schedule deleted successfully";
    }
}