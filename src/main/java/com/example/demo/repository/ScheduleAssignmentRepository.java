package com.example.demo.repository;

import com.example.demo.model.ScheduleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleAssignmentRepository
        extends JpaRepository<ScheduleAssignment, String> {
}