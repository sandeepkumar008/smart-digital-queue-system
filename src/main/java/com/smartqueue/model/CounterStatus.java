package com.smartqueue.model;

public enum CounterStatus {
    AVAILABLE("Available / Ready for Next"),
    BUSY("Serving Customer"),
    ON_BREAK("Operator on Break"),
    OFFLINE("Closed / Offline");

    private final String description;

    CounterStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
