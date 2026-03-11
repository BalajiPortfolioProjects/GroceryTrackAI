package com.grocerytrack.config;

import com.grocerytrack.repository.BudgetRepository;
import com.grocerytrack.repository.CategoryBudgetRepository;
import com.grocerytrack.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ReceiptRepository receiptRepository;
    private final BudgetRepository budgetRepository;
    private final CategoryBudgetRepository categoryBudgetRepository;

    @Override
    public void run(String... args) {
        log.info("ℹ️ Sample data loading disabled.");
    }
}
