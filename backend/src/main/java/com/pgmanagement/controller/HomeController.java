package com.pgmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
                "status", "RUNNING",
                "service", "PG Management System Backend API",
                "version", "1.0.0",
                "healthEndpoint", "/api/health",
                "timestamp", Instant.now().toString()
        ));
    }

    /**
     * Health-check endpoint used by Render to verify the service is alive.
     * Must return HTTP 200 quickly.
     */
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "PG Management System",
                "timestamp", Instant.now().toString()
        ));
    }
}
