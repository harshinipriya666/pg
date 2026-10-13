package com.pgmanagement.service;

import com.pgmanagement.entity.MealSchedule;
import com.pgmanagement.repository.MealScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MealScheduleService {

    private final MealScheduleRepository repository;

    public MealScheduleService(MealScheduleRepository repository) {
        this.repository = repository;
    }

    public List<MealSchedule> getAll() {
        return repository.findAll();
    }

    public List<MealSchedule> getByDate(LocalDate date) {
        return repository.findByScheduleDate(date);
    }

    public MealSchedule save(MealSchedule meal) {
        return repository.save(meal);
    }

    public MealSchedule update(Long id, MealSchedule data) {

        MealSchedule meal = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Meal schedule not found"));

        meal.setMealType(data.getMealType());
        meal.setMealTime(data.getMealTime());
        meal.setNotificationMessage(data.getNotificationMessage());
        meal.setScheduleDate(data.getScheduleDate());

        return repository.save(meal);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}