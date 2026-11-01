package com.smartqueue.controller;

import com.smartqueue.dto.CrowdForecastDto;
import com.smartqueue.repository.CrowdHistoryRepository;
import com.smartqueue.service.CrowdPredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/prediction")
@CrossOrigin(origins = "*")
public class CrowdPredictionController {

    private final CrowdPredictionService predictionService;
    private final CrowdHistoryRepository historyRepository;

    public CrowdPredictionController(CrowdPredictionService predictionService,
                                     CrowdHistoryRepository historyRepository) {
        this.predictionService = predictionService;
        this.historyRepository = historyRepository;
    }

    /**
     * Get 24-hour smart crowd prediction and peak analysis
     */
    @GetMapping("/forecast")
    public ResponseEntity<CrowdForecastDto> getForecast(
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) Long departmentId) {
        CrowdForecastDto forecast = predictionService.getForecast(day, departmentId);
        return ResponseEntity.ok(forecast);
    }

    /**
     * Get Day-of-Week historical influx trends (Monday to Sunday)
     */
    @GetMapping("/day-trends")
    public ResponseEntity<List<Map<String, Object>>> getDayTrends() {
        List<Object[]> raw = historyRepository.findDayOfWeekAverages();
        String[] dayNames = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : raw) {
            int dayNumber = ((Number) row[0]).intValue();
            double avgArrivals = Math.round(((Number) row[1]).doubleValue() * 10.0) / 10.0;
            double avgWait = Math.round(((Number) row[2]).doubleValue() * 10.0) / 10.0;

            Map<String, Object> map = new HashMap<>();
            map.put("dayNumber", dayNumber);
            map.put("dayName", dayNumber < dayNames.length ? dayNames[dayNumber] : "Day " + dayNumber);
            map.put("avgHourlyArrivals", avgArrivals);
            map.put("estimatedDailyFootfall", (int) (avgArrivals * 9)); // 9 operating hours
            map.put("avgWaitMinutes", avgWait);
            result.add(map);
        }

        return ResponseEntity.ok(result);
    }
}
