
package com.example.demo.controller;

import com.example.demo.service.SyntheticDataGeneratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/seed")
public class SeedController {

    private final SyntheticDataGeneratorService generatorService;

    public SeedController(
            SyntheticDataGeneratorService generatorService
    ) {
        this.generatorService = generatorService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> generate(
            @RequestParam(defaultValue = "small") String profile
    ) {
        return ResponseEntity.ok(
                generatorService.generate(profile)
        );
    }
}

