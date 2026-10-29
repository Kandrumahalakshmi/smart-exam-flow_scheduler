package com.example.demo.controller;

import com.example.demo.model.InvigilatorAvailability;
import com.example.demo.repository.InvigilatorAvailabilityRepository;
import com.example.demo.repository.InvigilatorRepository;
import com.example.demo.repository.TimeSlotRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/invigilator-availability")
public class InvigilatorAvailabilityController {

    private final InvigilatorAvailabilityRepository availabilityRepository;
    private final InvigilatorRepository invigilatorRepository;
    private final TimeSlotRepository timeSlotRepository;

    public InvigilatorAvailabilityController(
            InvigilatorAvailabilityRepository availabilityRepository,
            InvigilatorRepository invigilatorRepository,
            TimeSlotRepository timeSlotRepository) {
        this.availabilityRepository = availabilityRepository;
        this.invigilatorRepository = invigilatorRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    @GetMapping
    public List<InvigilatorAvailability> getAll() {
        return availabilityRepository.findAll();
    }

    @PostMapping
    public InvigilatorAvailability create(
            @RequestBody InvigilatorAvailability availability) {

        if (availability.getInvigilator() == null
                || availability.getInvigilator().getId() == null
                || availability.getTimeSlot() == null
                || availability.getTimeSlot().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invigilator ID and time slot ID are required");
        }

        var invigilator = invigilatorRepository
                .findById(availability.getInvigilator().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Invigilator not found"));

        var timeSlot = timeSlotRepository
                .findById(availability.getTimeSlot().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Time slot not found"));

        availability.setInvigilator(invigilator);
        availability.setTimeSlot(timeSlot);

        return availabilityRepository.save(availability);
    }
}