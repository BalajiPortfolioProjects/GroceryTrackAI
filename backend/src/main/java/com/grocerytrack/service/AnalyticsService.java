package com.grocerytrack.service;

import com.grocerytrack.dto.AnalyticsDTO;
import com.grocerytrack.dto.DashboardDTO;
import com.grocerytrack.model.Receipt;
import com.grocerytrack.repository.ReceiptItemRepository;
import com.grocerytrack.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptItemRepository receiptItemRepository;
    private final BudgetService budgetService;

    public AnalyticsDTO getAnalytics(String period) {
        LocalDate today = LocalDate.now();
        LocalDate start = switch (period.toLowerCase()) {
            case "month" ->
                today.minusDays(29);
            case "3months" ->
                today.minusDays(89);
            default ->
                today.minusDays(6); // week
        };

        BigDecimal totalSpent = receiptRepository.sumTotalBetween(start, today);
        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }

        BigDecimal weeklyBudget = budgetService.getBudget().getWeeklyAmount();
        BigDecimal remaining = weeklyBudget.subtract(totalSpent);
        int budgetPct = weeklyBudget.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.multiply(BigDecimal.valueOf(100))
                        .divide(weeklyBudget, 0, RoundingMode.HALF_UP).intValue()
                : 0;

        List<Receipt> receipts = receiptRepository.findByReceiptDateBetweenOrderByReceiptDateDesc(start, today);
        long tripCount = receipts.size();
        BigDecimal avgPerTrip = tripCount > 0
                ? totalSpent.divide(BigDecimal.valueOf(tripCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Chart data
        List<AnalyticsDTO.ChartPointDTO> chartData = buildChartData(period, receipts, start, today);

        // Category breakdown
        List<Object[]> catData = receiptItemRepository.sumByCategory(start, today);
        Map<String, BigDecimal> catMap = catData.stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (BigDecimal) r[1]));
        List<DashboardDTO.CategoryBreakdownDTO> categories
                = DashboardService.buildCategoryBreakdown(catMap, totalSpent);

        return AnalyticsDTO.builder()
                .totalSpent(totalSpent)
                .remaining(remaining)
                .avgPerTrip(avgPerTrip)
                .budgetPct(budgetPct)
                .chartData(chartData)
                .categories(categories)
                .build();
    }

    private List<AnalyticsDTO.ChartPointDTO> buildChartData(
            String period, List<Receipt> receipts, LocalDate start, LocalDate today) {

        // Group receipts by date → sum total
        Map<LocalDate, BigDecimal> byDate = receipts.stream().collect(
                Collectors.groupingBy(Receipt::getReceiptDate,
                        Collectors.reducing(BigDecimal.ZERO, Receipt::getTotal, BigDecimal::add)));

        List<AnalyticsDTO.ChartPointDTO> points = new ArrayList<>();

        switch (period.toLowerCase()) {
            case "month" -> {
                // 4 weeks
                for (int w = 3; w >= 0; w--) {
                    LocalDate wEnd = today.minusWeeks(w);
                    LocalDate wStart = wEnd.minusDays(6);
                    BigDecimal sum = byDate.entrySet().stream()
                            .filter(e -> !e.getKey().isBefore(wStart) && !e.getKey().isAfter(wEnd))
                            .map(Map.Entry::getValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    points.add(new AnalyticsDTO.ChartPointDTO("W" + (4 - w), sum.setScale(2, RoundingMode.HALF_UP)));
                }
            }
            case "3months" -> {
                // 3 months
                for (int m = 2; m >= 0; m--) {
                    LocalDate mEnd = today.minusMonths(m);
                    LocalDate mStart = mEnd.withDayOfMonth(1);
                    BigDecimal sum = byDate.entrySet().stream()
                            .filter(e -> !e.getKey().isBefore(mStart) && !e.getKey().isAfter(mEnd))
                            .map(Map.Entry::getValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    String label = mStart.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                    points.add(new AnalyticsDTO.ChartPointDTO(label, sum.setScale(2, RoundingMode.HALF_UP)));
                }
            }
            default -> {
                // 7 days (Mon–Sun relative to today)
                LocalDate monday = today.with(DayOfWeek.MONDAY);
                for (int d = 0; d < 7; d++) {
                    LocalDate day = monday.plusDays(d);
                    BigDecimal amt = byDate.getOrDefault(day, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
                    String label = day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                    points.add(new AnalyticsDTO.ChartPointDTO(label, amt));
                }
            }
        }
        return points;
    }
}
