package com.smartqueue.dto;

import java.util.Map;

public class SystemStatsDto {
    private long totalTicketsToday;
    private long waitingCount;
    private long servingCount;
    private long completedCount;
    private long noShowCount;
    private double avgWaitTimeMinutes;
    private int totalDepartments;
    private int totalCounters;
    private int onlineCounters;
    private Map<String, Long> ticketsPerDepartment;

    public SystemStatsDto() {}

    public long getTotalTicketsToday() { return totalTicketsToday; }
    public void setTotalTicketsToday(long totalTicketsToday) { this.totalTicketsToday = totalTicketsToday; }

    public long getWaitingCount() { return waitingCount; }
    public void setWaitingCount(long waitingCount) { this.waitingCount = waitingCount; }

    public long getServingCount() { return servingCount; }
    public void setServingCount(long servingCount) { this.servingCount = servingCount; }

    public long getCompletedCount() { return completedCount; }
    public void setCompletedCount(long completedCount) { this.completedCount = completedCount; }

    public long getNoShowCount() { return noShowCount; }
    public void setNoShowCount(long noShowCount) { this.noShowCount = noShowCount; }

    public double getAvgWaitTimeMinutes() { return avgWaitTimeMinutes; }
    public void setAvgWaitTimeMinutes(double avgWaitTimeMinutes) { this.avgWaitTimeMinutes = avgWaitTimeMinutes; }

    public int getTotalDepartments() { return totalDepartments; }
    public void setTotalDepartments(int totalDepartments) { this.totalDepartments = totalDepartments; }

    public int getTotalCounters() { return totalCounters; }
    public void setTotalCounters(int totalCounters) { this.totalCounters = totalCounters; }

    public int getOnlineCounters() { return onlineCounters; }
    public void setOnlineCounters(int onlineCounters) { this.onlineCounters = onlineCounters; }

    public Map<String, Long> getTicketsPerDepartment() { return ticketsPerDepartment; }
    public void setTicketsPerDepartment(Map<String, Long> ticketsPerDepartment) { this.ticketsPerDepartment = ticketsPerDepartment; }
}
