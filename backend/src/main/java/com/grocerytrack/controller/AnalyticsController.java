package com.grocerytrack.controller;

import com.grocerytrack.dto.AnalyticsDTO;
import com.grocerytrack.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * GET /api/analytics?period=week|month|3months
     */
    @GetMapping
    public ResponseEntity<AnalyticsDTO> getAnalytics(
            @RequestParam(defaultValue = "week") String period) {
        return ResponseEntity.ok(analyticsService.getAnalytics(period));
    }
}
