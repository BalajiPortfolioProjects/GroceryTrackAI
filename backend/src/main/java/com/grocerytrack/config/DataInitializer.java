package com.grocerytrack.config;

import com.grocerytrack.dto.BudgetDTO;
import com.grocerytrack.model.Budget;
import com.grocerytrack.model.CategoryBudget;
import com.grocerytrack.model.Receipt;
import com.grocerytrack.model.ReceiptItem;
import com.grocerytrack.repository.BudgetRepository;
import com.grocerytrack.repository.CategoryBudgetRepository;
import com.grocerytrack.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ReceiptRepository receiptRepository;
    private final BudgetRepository budgetRepository;
    private final CategoryBudgetRepository categoryBudgetRepository;

    @Override
    public void run(String... args) {
        seedBudget();
        seedReceipts();
        log.info("✅ Sample data loaded — H2 console at http://localhost:8080/h2-console");
    }

    private void seedBudget() {
        Budget budget = new Budget();
        budget.setWeeklyAmount(new BigDecimal("200.00"));
        budgetRepository.save(budget);

        Map<String, BigDecimal> cats = Map.of(
                "Produce", new BigDecimal("60.00"),
                "Dairy & Eggs", new BigDecimal("44.00"),
                "Meat & Seafood", new BigDecimal("40.00"),
                "Pantry & Snacks", new BigDecimal("36.00"),
                "Beverages", new BigDecimal("20.00")
        );
        cats.forEach((cat, amount) -> {
            CategoryBudget cb = new CategoryBudget();
            cb.setCategory(cat);
            cb.setLimitAmount(amount);
            categoryBudgetRepository.save(cb);
        });
    }

    private void seedReceipts() {
        LocalDate today = LocalDate.now();

        // Receipt 1 — Whole Foods
        Receipt wf = makeReceipt("Whole Foods Market", today.minusDays(2),
                new BigDecimal("67.42"), "Produce", "#16A34A", "#F0FDF4");
        wf.addItem(item("Organic Spinach 5oz", "Produce", "#16A34A", "#F0FDF4", "4.99"));
        wf.addItem(item("Free Range Eggs (12ct)", "Dairy & Eggs", "#2563EB", "#EFF6FF", "6.49"));
        wf.addItem(item("Wild Salmon Fillet 1lb", "Meat & Seafood", "#DC2626", "#FEF2F2", "14.99"));
        wf.addItem(item("Whole Milk 1gal", "Dairy & Eggs", "#2563EB", "#EFF6FF", "5.29"));
        wf.addItem(item("Roma Tomatoes 2lb", "Produce", "#16A34A", "#F0FDF4", "3.49"));
        wf.addItem(item("Greek Yogurt 32oz", "Dairy & Eggs", "#2563EB", "#EFF6FF", "6.99"));
        wf.addItem(item("Baby Carrots 1lb", "Produce", "#16A34A", "#F0FDF4", "2.29"));
        wf.addItem(item("Cheddar Cheese 8oz", "Dairy & Eggs", "#2563EB", "#EFF6FF", "4.79"));
        wf.addItem(item("Avocados (3ct)", "Produce", "#16A34A", "#F0FDF4", "4.49"));
        wf.addItem(item("Atlantic Cod Fillet", "Meat & Seafood", "#DC2626", "#FEF2F2", "8.99"));
        wf.addItem(item("Broccoli Crowns", "Produce", "#16A34A", "#F0FDF4", "2.49"));
        wf.addItem(item("Butter (4 sticks)", "Dairy & Eggs", "#2563EB", "#EFF6FF", "2.12"));
        wf.setItemsCount(12);
        receiptRepository.save(wf);

        // Receipt 2 — Trader Joe's
        Receipt tj = makeReceipt("Trader Joe's", today.minusDays(4),
                new BigDecimal("34.20"), "Dairy & Eggs", "#2563EB", "#EFF6FF");
        tj.addItem(item("Almond Milk 64oz", "Dairy & Eggs", "#2563EB", "#EFF6FF", "3.99"));
        tj.addItem(item("Mixed Nuts 16oz", "Pantry & Snacks", "#F59E0B", "#FFFBEB", "7.99"));
        tj.addItem(item("Sparkling Water 12pk", "Beverages", "#8B5CF6", "#F5F3FF", "4.49"));
        tj.addItem(item("Dark Chocolate Bar", "Pantry & Snacks", "#F59E0B", "#FFFBEB", "2.49"));
        tj.addItem(item("Frozen Brown Rice", "Pantry & Snacks", "#F59E0B", "#FFFBEB", "2.29"));
        tj.addItem(item("Goat Cheese 4oz", "Dairy & Eggs", "#2563EB", "#EFF6FF", "4.49"));
        tj.addItem(item("Cage-Free Eggs (6ct)", "Dairy & Eggs", "#2563EB", "#EFF6FF", "3.99"));
        tj.addItem(item("Orange Juice 64oz", "Beverages", "#8B5CF6", "#F5F3FF", "4.47"));
        tj.setItemsCount(8);
        receiptRepository.save(tj);

        // Receipt 3 — Costco
        Receipt co = makeReceipt("Costco", today.minusDays(6),
                new BigDecimal("26.23"), "Meat & Seafood", "#DC2626", "#FEF2F2");
        co.addItem(item("Ground Beef 3lb", "Meat & Seafood", "#DC2626", "#FEF2F2", "12.99"));
        co.addItem(item("Chicken Thighs 4lb", "Meat & Seafood", "#DC2626", "#FEF2F2", "9.99"));
        co.addItem(item("Pork Tenderloin", "Meat & Seafood", "#DC2626", "#FEF2F2", "3.25"));
        co.setItemsCount(3);
        receiptRepository.save(co);
    }

    private Receipt makeReceipt(String store, LocalDate date, BigDecimal total,
            String cat, String color, String bg) {
        Receipt r = new Receipt();
        r.setStore(store);
        r.setReceiptDate(date);
        r.setTotal(total);
        r.setPrimaryCategory(cat);
        r.setCategoryColor(color);
        r.setCategoryBg(bg);
        r.setCreatedAt(date.atTime(10, 0));
        return r;
    }

    private ReceiptItem item(String name, String cat, String color, String bg, String price) {
        ReceiptItem i = new ReceiptItem();
        i.setName(name);
        i.setCategory(cat);
        i.setCategoryColor(color);
        i.setCategoryBg(bg);
        i.setPrice(new BigDecimal(price));
        return i;
    }
}
