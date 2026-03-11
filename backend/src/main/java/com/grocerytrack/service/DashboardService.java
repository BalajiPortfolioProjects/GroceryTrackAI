package com.grocerytrack.service;

import com.grocerytrack.dto.DashboardDTO;
import com.grocerytrack.model.Receipt;
import com.grocerytrack.repository.ReceiptItemRepository;
import com.grocerytrack.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptItemRepository receiptItemRepository;
    private final BudgetService budgetService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d");

    // Category display config: key → (display name, color)
    private static final List<String[]> CATEGORIES = List.of(
            new String[]{"Produce", "Produce & Vegetables", "#22C55E"},
            new String[]{"Dairy & Eggs", "Dairy & Eggs", "#3B82F6"},
            new String[]{"Meat & Seafood", "Meat & Seafood", "#EF4444"},
            new String[]{"Pantry & Snacks", "Pantry & Snacks", "#F59E0B"},
            new String[]{"Beverages", "Beverages", "#8B5CF6"},
            new String[]{"Other", "Other", "#6B7280"}
    );

    public DashboardDTO getDashboard() {
        LocalDate weekStart = LocalDate.now().minusDays(6);
        LocalDate today = LocalDate.now();

        // Weekly totals
        BigDecimal weeklySpent = receiptRepository.sumTotalBetween(weekStart, today);
        if (weeklySpent == null) {
            weeklySpent = BigDecimal.ZERO;
        }

        Long items = receiptRepository.sumItemsBetween(weekStart, today);
        long itemsTracked = items != null ? items : 0;

        List<Receipt> weekReceipts = receiptRepository.findByReceiptDateBetweenOrderByReceiptDateDesc(weekStart, today);
        long receiptsScanned = weekReceipts.size();

        // Budget
        BigDecimal weeklyBudget = budgetService.getBudget().getWeeklyAmount();
        BigDecimal remaining = weeklyBudget.subtract(weeklySpent);
        int budgetPct = weeklyBudget.compareTo(BigDecimal.ZERO) > 0
                ? weeklySpent.multiply(BigDecimal.valueOf(100))
                        .divide(weeklyBudget, 0, RoundingMode.HALF_UP).intValue()
                : 0;

        // Category breakdown
        List<Object[]> catData = receiptItemRepository.sumByCategory(weekStart, today);
        Map<String, BigDecimal> catMap = catData.stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (BigDecimal) r[1]));

        List<DashboardDTO.CategoryBreakdownDTO> categories = buildCategoryBreakdown(catMap, weeklySpent);

        // Recent transactions (all receipts, max 10)
        List<Receipt> allReceipts = receiptRepository.findAllByOrderByReceiptDateDescCreatedAtDesc();
        List<DashboardDTO.TransactionDTO> transactions = allReceipts.stream()
                .limit(10)
                .map(r -> DashboardDTO.TransactionDTO.builder()
                .id(r.getId())
                .store(r.getStore())
                .date(r.getReceiptDate().format(DATE_FMT))
                .total(r.getTotal())
                .items(r.getItemsCount())
                .category(r.getPrimaryCategory())
                .categoryColor(r.getCategoryColor())
                .categoryBg(r.getCategoryBg())
                .build())
                .collect(Collectors.toList());

        return DashboardDTO.builder()
                .weeklySpent(weeklySpent)
                .weeklyBudget(weeklyBudget)
                .itemsTracked(itemsTracked)
                .receiptsScanned(receiptsScanned)
                .budgetPct(budgetPct)
                .remaining(remaining)
                .categories(categories)
                .recentTransactions(transactions)
                .build();
    }

    static List<DashboardDTO.CategoryBreakdownDTO> buildCategoryBreakdown(
            Map<String, BigDecimal> catMap, BigDecimal total) {

        return CATEGORIES.stream()
                .map(c -> {
                    BigDecimal amt = catMap.getOrDefault(c[0], BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
                    int pct = total != null && total.compareTo(BigDecimal.ZERO) > 0
                            ? amt.multiply(BigDecimal.valueOf(100))
                                    .divide(total, 0, RoundingMode.HALF_UP).intValue()
                            : 0;
                    return DashboardDTO.CategoryBreakdownDTO.builder()
                            .name(c[1])
                            .color(c[2])
                            .amount(amt)
                            .pct(pct)
                            .build();
                })
                .filter(c -> c.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
    }
}
