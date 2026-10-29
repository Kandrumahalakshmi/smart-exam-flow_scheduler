
package com.example.demo.controller;

import com.example.demo.model.Invigilator;
import com.example.demo.repository.InvigilatorRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invigilators")
public class InvigilatorController {

    private final InvigilatorRepository invigilatorRepository;

    public InvigilatorController(
            InvigilatorRepository invigilatorRepository) {
        this.invigilatorRepository = invigilatorRepository;
    }

    @GetMapping
    public List<Invigilator> getAllInvigilators() {
        return invigilatorRepository.findAll();
    }

    @PostMapping
    public Invigilator createInvigilator(
            @RequestBody Invigilator invigilator) {
        return invigilatorRepository.save(invigilator);
    }
}
