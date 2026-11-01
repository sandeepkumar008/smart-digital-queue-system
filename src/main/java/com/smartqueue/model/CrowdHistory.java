package com.smartqueue.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "crowd_history", indexes = {
    @Index(name = "idx_history_day_hour", columnList = "day_of_week, hour_of_day"),
    @Index(name = "idx_history_date", columnList = "record_date")
})
public class CrowdHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek; // 1 = Monday, 7 = Sunday

    @Column(name = "hour_of_day", nullable = false)
    private int hourOfDay; // 8 to 18 (e.g. 9 = 9:00 AM)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "total_arrivals", nullable = false)
    private int totalArrivals;

    @Column(name = "total_served", nullable = false)
    private int totalServed;

    @Column(name = "avg_wait_minutes", nullable = false)
    private double avgWaitMinutes;

    @Column(name = "avg_service_minutes", nullable = false)
    private double avgServiceMinutes;

    @Column(name = "peak_queue_length", nullable = false)
    private int peakQueueLength;

    @Column(name = "congestion_level", length = 20)
    private String congestionLevel; // "LOW", "MODERATE", "HIGH", "CRITICAL"

    public CrowdHistory() {}

    public CrowdHistory(LocalDate recordDate, int dayOfWeek, int hourOfDay, Department department,
                        int totalArrivals, int totalServed, double avgWaitMinutes, double avgServiceMinutes,
                        int peakQueueLength, String congestionLevel) {
        this.recordDate = recordDate;
        this.dayOfWeek = dayOfWeek;
        this.hourOfDay = hourOfDay;
        this.department = department;
        this.totalArrivals = totalArrivals;
        this.totalServed = totalServed;
        this.avgWaitMinutes = avgWaitMinutes;
        this.avgServiceMinutes = avgServiceMinutes;
        this.peakQueueLength = peakQueueLength;
        this.congestionLevel = congestionLevel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public int getTotalArrivals() {
        return totalArrivals;
    }

    public void setTotalArrivals(int totalArrivals) {
        this.totalArrivals = totalArrivals;
    }

    public int getTotalServed() {
        return totalServed;
    }

    public void setTotalServed(int totalServed) {
        this.totalServed = totalServed;
    }

    public double getAvgWaitMinutes() {
        return avgWaitMinutes;
    }

    public void setAvgWaitMinutes(double avgWaitMinutes) {
        this.avgWaitMinutes = avgWaitMinutes;
    }

    public double getAvgServiceMinutes() {
        return avgServiceMinutes;
    }

    public void setAvgServiceMinutes(double avgServiceMinutes) {
        this.avgServiceMinutes = avgServiceMinutes;
    }

    public int getPeakQueueLength() {
        return peakQueueLength;
    }

    public void setPeakQueueLength(int peakQueueLength) {
        this.peakQueueLength = peakQueueLength;
    }

    public String getCongestionLevel() {
        return congestionLevel;
    }

    public void setCongestionLevel(String congestionLevel) {
        this.congestionLevel = congestionLevel;
    }
}
