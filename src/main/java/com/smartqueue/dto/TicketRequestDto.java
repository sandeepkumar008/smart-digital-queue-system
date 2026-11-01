package com.smartqueue.dto;

import com.smartqueue.model.PriorityLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TicketRequestDto {

    @NotBlank(message = "Department code is required")
    private String departmentCode;

    private String customerName;

    private String customerPhone;

    @NotNull(message = "Priority level is required")
    private PriorityLevel priority = PriorityLevel.REGULAR;

    public TicketRequestDto() {}

    public TicketRequestDto(String departmentCode, String customerName, String customerPhone, PriorityLevel priority) {
        this.departmentCode = departmentCode;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.priority = priority != null ? priority : PriorityLevel.REGULAR;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
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
}
