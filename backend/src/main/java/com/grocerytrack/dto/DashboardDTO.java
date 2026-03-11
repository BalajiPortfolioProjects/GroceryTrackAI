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
public class DashboardDTO {

    private BigDecimal weeklySpent;
    private BigDecimal weeklyBudget;
    private long itemsTracked;
    private long receiptsScanned;
    private int budgetPct;
    private BigDecimal remaining;
    private List<CategoryBreakdownDTO> categories;
    private List<TransactionDTO> recentTransactions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdownDTO {

        private String name;
        private String color;
        private BigDecimal amount;
        private int pct;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionDTO {

        private Long id;
        private String store;
        private String date;
        private BigDecimal total;
        private int items;
        private String category;
        private String categoryColor;
        private String categoryBg;
    }
}
