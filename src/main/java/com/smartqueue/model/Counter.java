package com.smartqueue.model;

import jakarta.persistence.*;

@Entity
@Table(name = "counters")
public class Counter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "counter_number", nullable = false, unique = true)
    private int counterNumber;

    @Column(name = "counter_name", nullable = false, length = 50)
    private String counterName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "operator_name", length = 100)
    private String operatorName = "Staff Member";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CounterStatus status = CounterStatus.AVAILABLE;

    @Column(name = "total_served_today")
    private int totalServedToday = 0;

    public Counter() {}

    public Counter(int counterNumber, String counterName, Department department, String operatorName) {
        this.counterNumber = counterNumber;
        this.counterName = counterName;
        this.department = department;
        this.operatorName = operatorName;
        this.status = CounterStatus.AVAILABLE;
        this.totalServedToday = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCounterNumber() {
        return counterNumber;
    }

    public void setCounterNumber(int counterNumber) {
        this.counterNumber = counterNumber;
    }

    public String getCounterName() {
        return counterName;
    }

    public void setCounterName(String counterName) {
        this.counterName = counterName;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public CounterStatus getStatus() {
        return status;
    }

    public void setStatus(CounterStatus status) {
        this.status = status;
    }

    public int getTotalServedToday() {
        return totalServedToday;
    }

    public void setTotalServedToday(int totalServedToday) {
        this.totalServedToday = totalServedToday;
    }

    public void incrementServed() {
        this.totalServedToday++;
    }
}
