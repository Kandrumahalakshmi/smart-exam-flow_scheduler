
package com.example.demo.controller;

import com.example.demo.service.SeedValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/seed")
public class SeedValidationController {

    private final SeedValidationService validationService;

    public SeedValidationController(SeedValidationService validationService) {
        this.validationService = validationService;
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate() {
        Map<String, Object> result = validationService.validate();

        if (Boolean.FALSE.equals(result.get("valid"))) {
            return ResponseEntity.unprocessableEntity().body(result);
        }

        return ResponseEntity.ok(result);
    }
}
