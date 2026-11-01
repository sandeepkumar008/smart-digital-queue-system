package com.smartqueue.model;

public enum TicketStatus {
    WAITING("Waiting in Queue"),
    SERVING("Being Served at Counter"),
    COMPLETED("Service Completed"),
    NO_SHOW("No Show / Skipped"),
    CANCELLED("Cancelled");

    private final String description;

    TicketStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
