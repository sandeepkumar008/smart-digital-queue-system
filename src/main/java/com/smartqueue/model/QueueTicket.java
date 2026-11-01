package com.smartqueue.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue_tickets", indexes = {
    @Index(name = "idx_ticket_token", columnList = "token_number"),
    @Index(name = "idx_ticket_status", columnList = "status"),
    @Index(name = "idx_ticket_issued_at", columnList = "issued_at")
})
public class QueueTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_number", nullable = false, length = 20)
    private String tokenNumber;

    @Column(name = "sequence_number", nullable = false)
    private int sequenceNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "customer_name", length = 100)
    private String customerName = "Walk-in Guest";

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PriorityLevel priority = PriorityLevel.REGULAR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status = TicketStatus.WAITING;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "counter_id")
    private Counter counter;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt = LocalDateTime.now();

    @Column(name = "called_at")
    private LocalDateTime calledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "estimated_wait_minutes")
    private int estimatedWaitMinutes = 0;

    @Column(name = "actual_wait_minutes")
    private Integer actualWaitMinutes;

    @Column(name = "actual_service_minutes")
    private Integer actualServiceMinutes;

    @Column(length = 255)
    private String notes;

    public QueueTicket() {}

    public QueueTicket(String tokenNumber, int sequenceNumber, Department department, String customerName,
                       String customerPhone, PriorityLevel priority, int estimatedWaitMinutes) {
        this.tokenNumber = tokenNumber;
        this.sequenceNumber = sequenceNumber;
        this.department = department;
        this.customerName = customerName != null && !customerName.isBlank() ? customerName : "Walk-in Guest";
        this.customerPhone = customerPhone;
        this.priority = priority != null ? priority : PriorityLevel.REGULAR;
        this.status = TicketStatus.WAITING;
        this.issuedAt = LocalDateTime.now();
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(String tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public PriorityLevel getPriority() {
        return priority;
    }

    public void setPriority(PriorityLevel priority) {
        this.priority = priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public Counter getCounter() {
        return counter;
    }

    public void setCounter(Counter counter) {
        this.counter = counter;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getCalledAt() {
        return calledAt;
    }

    public void setCalledAt(LocalDateTime calledAt) {
        this.calledAt = calledAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public int getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }

    public void setEstimatedWaitMinutes(int estimatedWaitMinutes) {
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }

    public Integer getActualWaitMinutes() {
        return actualWaitMinutes;
    }

    public void setActualWaitMinutes(Integer actualWaitMinutes) {
        this.actualWaitMinutes = actualWaitMinutes;
    }

    public Integer getActualServiceMinutes() {
        return actualServiceMinutes;
    }

    public void setActualServiceMinutes(Integer actualServiceMinutes) {
        this.actualServiceMinutes = actualServiceMinutes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
