package com.smartqueue.model;

public enum PriorityLevel {
    REGULAR(1, "Regular", 1.0),
    SENIOR_CITIZEN(2, "Senior Citizen / Specially-Abled", 1.5),
    VIP_URGENT(3, "Urgent / Fast Track", 2.0);

    private final int weight;
    private final String displayName;
    private final double priorityMultiplier;

    PriorityLevel(int weight, String displayName, double priorityMultiplier) {
        this.weight = weight;
        this.displayName = displayName;
        this.priorityMultiplier = priorityMultiplier;
    }

    public int getWeight() {
        return weight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getPriorityMultiplier() {
        return priorityMultiplier;
    }
}
