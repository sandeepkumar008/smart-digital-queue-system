package com.smartqueue.controller;

import com.smartqueue.dto.SystemStatsDto;
import com.smartqueue.service.DatabaseSeederService;
import com.smartqueue.service.QueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final QueueService queueService;
    private final DatabaseSeederService seederService;

    public AdminController(QueueService queueService, DatabaseSeederService seederService) {
        this.queueService = queueService;
        this.seederService = seederService;
    }

    /**
     * Get system-wide operational metrics
     */
    @GetMapping("/stats")
    public ResponseEntity<SystemStatsDto> getSystemStats() {
        return ResponseEntity.ok(queueService.getSystemStats());
    }

    /**
     * Reset and re-seed the database with fresh demo data
     */
    @PostMapping("/reseed")
    public ResponseEntity<Map<String, String>> reseedDatabase() {
        seederService.resetAndReseed();
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Database successfully reset and re-seeded with demo records, 30-day crowd history, and live queue."
        ));
    }
}
