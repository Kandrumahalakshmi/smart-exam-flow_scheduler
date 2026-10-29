package com.example.demo.controller;

import com.example.demo.model.AssignmentStatus;
import com.example.demo.repository.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final StudentRepository students;
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;
    private final ExamRepository exams;
    private final RoomRepository rooms;
    private final TimeSlotRepository slots;
    private final InvigilatorRepository invigilators;
    private final ScheduleAssignmentRepository assignments;

    public DashboardController(StudentRepository students, CourseRepository courses,
            EnrollmentRepository enrollments, ExamRepository exams, RoomRepository rooms,
            TimeSlotRepository slots, InvigilatorRepository invigilators,
            ScheduleAssignmentRepository assignments) {
        this.students = students;
        this.courses = courses;
        this.enrollments = enrollments;
        this.exams = exams;
        this.rooms = rooms;
        this.slots = slots;
        this.invigilators = invigilators;
        this.assignments = assignments;
    }

    @GetMapping
    public Map<String, Object> summary() {
        var allAssignments = assignments.findAll();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("students", students.count());
        result.put("courses", courses.count());
        result.put("enrollments", enrollments.count());
        result.put("exams", exams.count());
        result.put("rooms", rooms.count());
        result.put("timeSlots", slots.count());
        result.put("invigilators", invigilators.count());
        result.put("assignments", allAssignments.size());
        result.put("assigned", allAssignments.stream().filter(a -> a.getStatus() == AssignmentStatus.ASSIGNED).count());
        result.put("unassigned", allAssignments.stream().filter(a -> a.getStatus() == AssignmentStatus.UNASSIGNED).count());
        return result;
    }
}
