package com.example.demo.controller;

import com.example.demo.model.RoomEquipment;
import com.example.demo.repository.RoomEquipmentRepository;
import com.example.demo.repository.RoomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/room-equipment")
public class RoomEquipmentController {
    private final RoomEquipmentRepository repository;
    private final RoomRepository rooms;

    public RoomEquipmentController(RoomEquipmentRepository repository, RoomRepository rooms) {
        this.repository = repository;
        this.rooms = rooms;
    }

    @GetMapping
    public List<RoomEquipment> all() { return repository.findAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomEquipment create(@RequestBody RoomEquipment item) {
        if (item.getId() == null || item.getId().isBlank() || item.getRoom() == null || item.getRoom().getId() == null || item.getEquipmentType() == null || item.getEquipmentType().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id, room.id, and equipmentType are required");
        }
        item.setRoom(rooms.findById(item.getRoom().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found")));
        return repository.save(item);
    }
}
