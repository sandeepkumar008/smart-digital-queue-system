package com.smartqueue.dto;

import java.util.List;

public class CrowdForecastDto {
    private String dayName;
    private int dayOfWeek;
    private Long departmentId;
    private String departmentName;
    private List<HourlyPredictionDto> hourlyPredictions;
    private String peakHourWindow;
    private String lowestCrowdWindow;
    private List<String> bestVisitSlots;
    private int totalPredictedDailyCrowd;
    private double avgDailyPredictedWaitMinutes;
    private String crowdAlertNotice;
    private String modelDescription;

    public CrowdForecastDto() {}

    public String getDayName() { return dayName; }
    public void setDayName(String dayName) { this.dayName = dayName; }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public List<HourlyPredictionDto> getHourlyPredictions() { return hourlyPredictions; }
    public void setHourlyPredictions(List<HourlyPredictionDto> hourlyPredictions) { this.hourlyPredictions = hourlyPredictions; }

    public String getPeakHourWindow() { return peakHourWindow; }
    public void setPeakHourWindow(String peakHourWindow) { this.peakHourWindow = peakHourWindow; }

    public String getLowestCrowdWindow() { return lowestCrowdWindow; }
    public void setLowestCrowdWindow(String lowestCrowdWindow) { this.lowestCrowdWindow = lowestCrowdWindow; }

    public List<String> getBestVisitSlots() { return bestVisitSlots; }
    public void setBestVisitSlots(List<String> bestVisitSlots) { this.bestVisitSlots = bestVisitSlots; }

    public int getTotalPredictedDailyCrowd() { return totalPredictedDailyCrowd; }
    public void setTotalPredictedDailyCrowd(int totalPredictedDailyCrowd) { this.totalPredictedDailyCrowd = totalPredictedDailyCrowd; }

    public double getAvgDailyPredictedWaitMinutes() { return avgDailyPredictedWaitMinutes; }
    public void setAvgDailyPredictedWaitMinutes(double avgDailyPredictedWaitMinutes) { this.avgDailyPredictedWaitMinutes = avgDailyPredictedWaitMinutes; }

    public String getCrowdAlertNotice() { return crowdAlertNotice; }
    public void setCrowdAlertNotice(String crowdAlertNotice) { this.crowdAlertNotice = crowdAlertNotice; }

    public String getModelDescription() { return modelDescription; }
    public void setModelDescription(String modelDescription) { this.modelDescription = modelDescription; }
}
