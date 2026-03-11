package com.grocerytrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDTO {

    private BigDecimal totalSpent;
    private BigDecimal remaining;
    private BigDecimal avgPerTrip;
    private int budgetPct;
    private List<ChartPointDTO> chartData;
    private List<DashboardDTO.CategoryBreakdownDTO> categories;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartPointDTO {

        private String label;
        private BigDecimal amount;
    }
}
