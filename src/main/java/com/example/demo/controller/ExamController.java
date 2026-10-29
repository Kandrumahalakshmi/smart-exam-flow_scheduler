
package com.example.demo.controller;

import com.example.demo.model.Exam;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.ExamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamRepository examRepository;
    private final CourseRepository courseRepository;

    public ExamController(
            ExamRepository examRepository,
            CourseRepository courseRepository) {
        this.examRepository = examRepository;
        this.courseRepository = courseRepository;
    }

    @GetMapping
    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    @PostMapping
    public Exam createExam(@RequestBody Exam exam) {
        if (exam.getCourse() == null || exam.getCourse().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Course ID is required");
        }

        var course = courseRepository.findById(exam.getCourse().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Course not found"));

        exam.setCourse(course);
        return examRepository.save(exam);
    }
}
