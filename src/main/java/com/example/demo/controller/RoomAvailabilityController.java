
package com.example.demo.controller;

import com.example.demo.model.RoomAvailability;
import com.example.demo.repository.RoomAvailabilityRepository;
import com.example.demo.repository.RoomRepository;
import com.example.demo.repository.TimeSlotRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/room-availability")
public class RoomAvailabilityController {

    private final RoomAvailabilityRepository availabilityRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;

    public RoomAvailabilityController(
            RoomAvailabilityRepository availabilityRepository,
            RoomRepository roomRepository,
            TimeSlotRepository timeSlotRepository) {
        this.availabilityRepository = availabilityRepository;
        this.roomRepository = roomRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    @GetMapping
    public List<RoomAvailability> getAll() {
        return availabilityRepository.findAll();
    }

    @PostMapping
    public RoomAvailability create(
            @RequestBody RoomAvailability availability) {

        if (availability.getRoom() == null
                || availability.getRoom().getId() == null
                || availability.getTimeSlot() == null
                || availability.getTimeSlot().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Room ID and time slot ID are required");
        }

        var room = roomRepository.findById(availability.getRoom().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room not found"));

        var timeSlot = timeSlotRepository
                .findById(availability.getTimeSlot().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Time slot not found"));

        availability.setRoom(room);
        availability.setTimeSlot(timeSlot);

        return availabilityRepository.save(availability);
    }
}
