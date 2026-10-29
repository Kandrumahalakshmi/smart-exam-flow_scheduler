package com.example.demo.controller;

import com.example.demo.model.Accommodation;
import com.example.demo.repository.AccommodationRepository;
import com.example.demo.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/accommodations")
public class AccommodationController {
    private final AccommodationRepository repository;
    private final StudentRepository students;

    public AccommodationController(AccommodationRepository repository, StudentRepository students) {
        this.repository = repository;
        this.students = students;
    }

    @GetMapping
    public List<Accommodation> all() { return repository.findAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Accommodation create(@RequestBody Accommodation item) {
        if (item.getId() == null || item.getId().isBlank() || item.getStudent() == null || item.getStudent().getId() == null || item.getType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id, student.id, and type are required");
        }
        item.setStudent(students.findById(item.getStudent().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found")));
        return repository.save(item);
    }
}
