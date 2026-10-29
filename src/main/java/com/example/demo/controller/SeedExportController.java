
package com.example.demo.controller;

import com.example.demo.service.SeedExportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/seed")
public class SeedExportController {

    private final SeedExportService exportService;

    public SeedExportController(SeedExportService exportService) {
        this.exportService = exportService;
    }

    @PostMapping("/export/csv")
    public ResponseEntity<Map<String, Object>> exportCsv() {
        return ResponseEntity.ok(exportService.exportCsv());
    }
}

