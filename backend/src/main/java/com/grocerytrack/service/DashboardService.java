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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    
    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final ReceiptRepository receiptRepository;
    private final ReceiptItemRepository receiptItemRepository;
    private final BudgetService budgetService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d");

    public DashboardDTO getDashboard() {
        LocalDate weekStart = LocalDate.now().minusDays(6);
        LocalDate today = LocalDate.now();

        // Weekly totals
        BigDecimal weeklySpent = receiptRepository.sumTotalBetween(weekStart, today);
        if (weeklySpent == null) {
            weeklySpent = BigDecimal.ZERO;
        }

        BigDecimal totalSpends = receiptRepository.sumTotal();
        if (totalSpends == null) {
            totalSpends = BigDecimal.ZERO;
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

        // Category breakdown summed directly from ReceiptItem prices
        List<Object[]> rawCatSums = receiptItemRepository.sumByCategoryAllTime();
        Map<String, BigDecimal> catAmounts = new LinkedHashMap<>();
        Map<String, String> catColors = new LinkedHashMap<>();
        for (Object[] row : rawCatSums) {
            String cat = (String) row[0];
            String color = (String) row[1];
            BigDecimal sum = (BigDecimal) row[2];
            catAmounts.merge(cat, sum, BigDecimal::add);
            catColors.putIfAbsent(cat, color);
        }

        List<DashboardDTO.CategoryBreakdownDTO> categories = buildCategoryBreakdownFromItems(catAmounts, catColors, totalSpends);
        
        log.info("Category amounts from ReceiptItems: {}", catAmounts);
        log.info("Total Spends: {}", totalSpends);
        log.info("Built categories breakdown: {}", categories);

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
                .totalSpends(totalSpends)
        		.itemsTracked(itemsTracked)
                .receiptsScanned(receiptsScanned)
                .budgetPct(budgetPct)
                .remaining(remaining)
                .categories(categories)
                .recentTransactions(transactions)
                .build();
    }

    static List<DashboardDTO.CategoryBreakdownDTO> buildCategoryBreakdownFromItems(
            Map<String, BigDecimal> catAmounts, Map<String, String> catColors, BigDecimal total) {

        return catAmounts.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .map(entry -> {
                    String cat = entry.getKey();
                    BigDecimal amt = entry.getValue().setScale(2, RoundingMode.HALF_UP);
                    String color = catColors.getOrDefault(cat, "#6B7280");
                    int pct = total != null && total.compareTo(BigDecimal.ZERO) > 0
                            ? amt.multiply(BigDecimal.valueOf(100))
                                    .divide(total, 0, RoundingMode.HALF_UP).intValue()
                            : 0;
                    return DashboardDTO.CategoryBreakdownDTO.builder()
                            .name(cat)
                            .color(color)
                            .amount(amt)
                            .pct(pct)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
