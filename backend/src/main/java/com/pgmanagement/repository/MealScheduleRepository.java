package com.pgmanagement.repository;

import com.pgmanagement.entity.MealSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MealScheduleRepository
        extends JpaRepository<MealSchedule, Long> {

    List<MealSchedule> findByScheduleDate(LocalDate scheduleDate);
}