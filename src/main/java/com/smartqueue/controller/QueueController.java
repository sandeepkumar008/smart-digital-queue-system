package com.smartqueue.controller;

import com.smartqueue.dto.*;
import com.smartqueue.model.Counter;
import com.smartqueue.model.CounterStatus;
import com.smartqueue.model.Department;
import com.smartqueue.model.QueueTicket;
import com.smartqueue.service.QueueService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/queue")
@CrossOrigin(origins = "*")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    /**
     * Issue a new digital queue ticket / token
     */
    @PostMapping("/ticket")
    public ResponseEntity<TicketResponseDto> issueTicket(@Valid @RequestBody TicketRequestDto request) {
        TicketResponseDto response = queueService.issueTicket(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Live display board stream/data for public TV screen
     */
    @GetMapping("/board")
    public ResponseEntity<LiveQueueBoardDto> getLiveBoard() {
        return ResponseEntity.ok(queueService.getLiveBoard());
    }

    /**
     * Customer tracks their personal token
     */
    @GetMapping("/track/{tokenNumber}")
    public ResponseEntity<TicketResponseDto> trackToken(@PathVariable String tokenNumber) {
        return ResponseEntity.ok(queueService.trackToken(tokenNumber));
    }

    /**
     * Counter operator calls the next ticket in line
     */
    @PostMapping("/counter/{counterId}/call-next")
    public ResponseEntity<TicketResponseDto> callNext(@PathVariable Long counterId) {
        TicketResponseDto ticket = queueService.callNext(counterId);
        return ResponseEntity.ok(ticket);
    }

    /**
     * Counter operator recalls the current ticket (triggers chime announcement)
     */
    @PostMapping("/counter/{counterId}/recall")
    public ResponseEntity<TicketResponseDto> recall(@PathVariable Long counterId) {
        TicketResponseDto ticket = queueService.recall(counterId);
        return ResponseEntity.ok(ticket);
    }

    /**
     * Counter operator completes the service for current ticket
     */
    @PostMapping("/counter/{counterId}/complete")
    public ResponseEntity<TicketResponseDto> complete(@PathVariable Long counterId) {
        TicketResponseDto ticket = queueService.completeCurrentTicket(counterId);
        return ResponseEntity.ok(ticket);
    }

    /**
     * Counter operator marks current ticket as No-Show
     */
    @PostMapping("/counter/{counterId}/no-show")
    public ResponseEntity<TicketResponseDto> markNoShow(@PathVariable Long counterId) {
        TicketResponseDto ticket = queueService.markNoShow(counterId);
        return ResponseEntity.ok(ticket);
    }

    /**
     * Update counter status (e.g. OFFLINE, AVAILABLE, ON_BREAK)
     */
    @PostMapping("/counter/{counterId}/status")
    public ResponseEntity<Counter> updateStatus(@PathVariable Long counterId, @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        CounterStatus status = CounterStatus.valueOf(statusStr.toUpperCase());
        Counter updated = queueService.updateCounterStatus(counterId, status);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get list of all departments
     */
    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getDepartments() {
        return ResponseEntity.ok(queueService.getAllDepartments());
    }

    /**
     * Get list of all physical service counters
     */
    @GetMapping("/counters")
    public ResponseEntity<List<Counter>> getCounters() {
        return ResponseEntity.ok(queueService.getAllCounters());
    }

    /**
     * Get currently waiting tickets (optionally filtered by department)
     */
    @GetMapping("/waiting")
    public ResponseEntity<List<QueueTicket>> getWaitingTickets(@RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(queueService.getWaitingTickets(departmentId));
    }
}
