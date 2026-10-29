
package com.example.demo.controller;

import com.example.demo.model.ScheduleAssignment;
import com.example.demo.repository.ScheduleAssignmentRepository;
import com.example.demo.service.SchedulingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final SchedulingService schedulingService;
    private final ScheduleAssignmentRepository assignmentRepository;

    public ScheduleController(
            SchedulingService schedulingService,
            ScheduleAssignmentRepository assignmentRepository) {
        this.schedulingService = schedulingService;
        this.assignmentRepository = assignmentRepository;
    }

    @PostMapping("/generate")
    public List<ScheduleAssignment> generateSchedule() {
        return schedulingService.generateSchedule();
    }

    @GetMapping
    public List<ScheduleAssignment> getSchedule() {
        return assignmentRepository.findAll();
    }
}
