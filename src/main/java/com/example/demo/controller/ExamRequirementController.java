package com.example.demo.controller;

import com.example.demo.model.ExamRequirement;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.ExamRequirementRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/exam-requirements")
public class ExamRequirementController {
    private final ExamRequirementRepository repository;
    private final ExamRepository exams;

    public ExamRequirementController(ExamRequirementRepository repository, ExamRepository exams) {
        this.repository = repository;
        this.exams = exams;
    }

    @GetMapping
    public List<ExamRequirement> all() { return repository.findAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExamRequirement create(@RequestBody ExamRequirement item) {
        if (item.getId() == null || item.getId().isBlank() || item.getExam() == null || item.getExam().getId() == null || item.getRequirementType() == null || item.getRequirementType().isBlank() || item.getRequirementValue() == null || item.getRequirementValue().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id, exam.id, requirementType, and requirementValue are required");
        }
        item.setExam(exams.findById(item.getExam().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found")));
        return repository.save(item);
    }
}
