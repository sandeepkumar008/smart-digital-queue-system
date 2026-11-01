package com.smartqueue.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class LiveQueueBoardDto {
    private LocalDateTime timestamp;
    private TicketResponseDto lastCalledTicket;
    private List<CounterDisplayDto> activeCounters;
    private List<TicketResponseDto> currentlyServing;
    private List<TicketResponseDto> nextUpTickets;
    private Map<String, Long> waitingCountByDepartment;
    private long totalWaiting;
    private long totalServing;
    private long totalServedToday;

    public static class CounterDisplayDto {
        private int counterNumber;
        private String counterName;
        private String departmentName;
        private String departmentColor;
        private String operatorName;
        private String status;
        private String currentToken;
        private String customerName;

        public CounterDisplayDto() {}

        public int getCounterNumber() { return counterNumber; }
        public void setCounterNumber(int counterNumber) { this.counterNumber = counterNumber; }

        public String getCounterName() { return counterName; }
        public void setCounterName(String counterName) { this.counterName = counterName; }

        public String getDepartmentName() { return departmentName; }
        public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

        public String getDepartmentColor() { return departmentColor; }
        public void setDepartmentColor(String departmentColor) { this.departmentColor = departmentColor; }

        public String getOperatorName() { return operatorName; }
        public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getCurrentToken() { return currentToken; }
        public void setCurrentToken(String currentToken) { this.currentToken = currentToken; }

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
    }

    public LiveQueueBoardDto() {
        this.timestamp = LocalDateTime.now();
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public TicketResponseDto getLastCalledTicket() { return lastCalledTicket; }
    public void setLastCalledTicket(TicketResponseDto lastCalledTicket) { this.lastCalledTicket = lastCalledTicket; }

    public List<CounterDisplayDto> getActiveCounters() { return activeCounters; }
    public void setActiveCounters(List<CounterDisplayDto> activeCounters) { this.activeCounters = activeCounters; }

    public List<TicketResponseDto> getCurrentlyServing() { return currentlyServing; }
    public void setCurrentlyServing(List<TicketResponseDto> currentlyServing) { this.currentlyServing = currentlyServing; }

    public List<TicketResponseDto> getNextUpTickets() { return nextUpTickets; }
    public void setNextUpTickets(List<TicketResponseDto> nextUpTickets) { this.nextUpTickets = nextUpTickets; }

    public Map<String, Long> getWaitingCountByDepartment() { return waitingCountByDepartment; }
    public void setWaitingCountByDepartment(Map<String, Long> waitingCountByDepartment) { this.waitingCountByDepartment = waitingCountByDepartment; }

    public long getTotalWaiting() { return totalWaiting; }
    public void setTotalWaiting(long totalWaiting) { this.totalWaiting = totalWaiting; }

    public long getTotalServing() { return totalServing; }
    public void setTotalServing(long totalServing) { this.totalServing = totalServing; }

    public long getTotalServedToday() { return totalServedToday; }
    public void setTotalServedToday(long totalServedToday) { this.totalServedToday = totalServedToday; }
}
