package com.smartqueue.dto;

public class HourlyPredictionDto {
    private int hourOfDay;          // 9 to 18
    private String timeLabel;       // e.g. "09:00 AM"
    private int predictedArrivals;  // forecasted visitors
    private double predictedWaitMinutes; // expected wait time
    private String congestionLevel; // "LOW", "MODERATE", "HIGH", "CRITICAL"
    private String congestionColor; // "#10b981", "#f59e0b", "#f97316", "#ef4444"
    private int recommendedCounters;// optimal active counters to keep wait < 15 min
    private double confidenceScore; // e.g. 0.92

    public HourlyPredictionDto() {}

    public HourlyPredictionDto(int hourOfDay, String timeLabel, int predictedArrivals,
                               double predictedWaitMinutes, String congestionLevel,
                               String congestionColor, int recommendedCounters, double confidenceScore) {
        this.hourOfDay = hourOfDay;
        this.timeLabel = timeLabel;
        this.predictedArrivals = predictedArrivals;
        this.predictedWaitMinutes = predictedWaitMinutes;
        this.congestionLevel = congestionLevel;
        this.congestionColor = congestionColor;
        this.recommendedCounters = recommendedCounters;
        this.confidenceScore = confidenceScore;
    }

    public int getHourOfDay() { return hourOfDay; }
    public void setHourOfDay(int hourOfDay) { this.hourOfDay = hourOfDay; }

    public String getTimeLabel() { return timeLabel; }
    public void setTimeLabel(String timeLabel) { this.timeLabel = timeLabel; }

    public int getPredictedArrivals() { return predictedArrivals; }
    public void setPredictedArrivals(int predictedArrivals) { this.predictedArrivals = predictedArrivals; }

    public double getPredictedWaitMinutes() { return predictedWaitMinutes; }
    public void setPredictedWaitMinutes(double predictedWaitMinutes) { this.predictedWaitMinutes = predictedWaitMinutes; }

    public String getCongestionLevel() { return congestionLevel; }
    public void setCongestionLevel(String congestionLevel) { this.congestionLevel = congestionLevel; }

    public String getCongestionColor() { return congestionColor; }
    public void setCongestionColor(String congestionColor) { this.congestionColor = congestionColor; }

    public int getRecommendedCounters() { return recommendedCounters; }
    public void setRecommendedCounters(int recommendedCounters) { this.recommendedCounters = recommendedCounters; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
}
