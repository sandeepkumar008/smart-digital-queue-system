package com.smartqueue.dto;

import com.smartqueue.model.PriorityLevel;
import com.smartqueue.model.TicketStatus;

import java.time.LocalDateTime;

public class TicketResponseDto {
    private Long id;
    private String tokenNumber;
    private int sequenceNumber;
    private Long departmentId;
    private String departmentCode;
    private String departmentName;
    private String departmentColor;
    private String customerName;
    private String customerPhone;
    private PriorityLevel priority;
    private String priorityLabel;
    private TicketStatus status;
    private String statusDescription;
    private Integer counterNumber;
    private String counterName;
    private LocalDateTime issuedAt;
    private LocalDateTime calledAt;
    private LocalDateTime completedAt;
    private int estimatedWaitMinutes;
    private long ticketsAhead;
    private Integer actualWaitMinutes;
    private Integer actualServiceMinutes;

    public TicketResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }

    public int getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getDepartmentColor() { return departmentColor; }
    public void setDepartmentColor(String departmentColor) { this.departmentColor = departmentColor; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public PriorityLevel getPriority() { return priority; }
    public void setPriority(PriorityLevel priority) { this.priority = priority; }

    public String getPriorityLabel() { return priorityLabel; }
    public void setPriorityLabel(String priorityLabel) { this.priorityLabel = priorityLabel; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public String getStatusDescription() { return statusDescription; }
    public void setStatusDescription(String statusDescription) { this.statusDescription = statusDescription; }

    public Integer getCounterNumber() { return counterNumber; }
    public void setCounterNumber(Integer counterNumber) { this.counterNumber = counterNumber; }

    public String getCounterName() { return counterName; }
    public void setCounterName(String counterName) { this.counterName = counterName; }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }

    public LocalDateTime getCalledAt() { return calledAt; }
    public void setCalledAt(LocalDateTime calledAt) { this.calledAt = calledAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public int getEstimatedWaitMinutes() { return estimatedWaitMinutes; }
    public void setEstimatedWaitMinutes(int estimatedWaitMinutes) { this.estimatedWaitMinutes = estimatedWaitMinutes; }

    public long getTicketsAhead() { return ticketsAhead; }
    public void setTicketsAhead(long ticketsAhead) { this.ticketsAhead = ticketsAhead; }

    public Integer getActualWaitMinutes() { return actualWaitMinutes; }
    public void setActualWaitMinutes(Integer actualWaitMinutes) { this.actualWaitMinutes = actualWaitMinutes; }

    public Integer getActualServiceMinutes() { return actualServiceMinutes; }
    public void setActualServiceMinutes(Integer actualServiceMinutes) { this.actualServiceMinutes = actualServiceMinutes; }
}
