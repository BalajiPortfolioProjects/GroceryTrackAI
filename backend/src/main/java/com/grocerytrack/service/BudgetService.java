package com.grocerytrack.service;

import com.grocerytrack.dto.BudgetDTO;
import com.grocerytrack.model.Budget;
import com.grocerytrack.model.CategoryBudget;
import com.grocerytrack.repository.BudgetRepository;
import com.grocerytrack.repository.CategoryBudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryBudgetRepository categoryBudgetRepository;

    public BudgetDTO getBudget() {
        Budget budget = budgetRepository.findById(1L).orElseGet(() -> {
            Budget b = new Budget();
            b.setWeeklyAmount(new BigDecimal("200.00"));
            return budgetRepository.save(b);
        });

        Map<String, BigDecimal> categories = categoryBudgetRepository.findAll().stream()
                .collect(Collectors.toMap(CategoryBudget::getCategory, CategoryBudget::getLimitAmount));

        return BudgetDTO.builder()
                .weeklyAmount(budget.getWeeklyAmount())
                .categories(categories)
                .build();
    }

    @Transactional
    public BudgetDTO setWeeklyBudget(BigDecimal amount) {
        Budget budget = budgetRepository.findById(1L).orElse(new Budget());
        budget.setWeeklyAmount(amount);
        budgetRepository.save(budget);
        return getBudget();
    }

    @Transactional
    public BudgetDTO setCategoryBudgets(Map<String, BigDecimal> categoryLimits) {
        for (Map.Entry<String, BigDecimal> entry : categoryLimits.entrySet()) {
            CategoryBudget cb = categoryBudgetRepository.findByCategory(entry.getKey())
                    .orElse(new CategoryBudget());
            cb.setCategory(entry.getKey());
            cb.setLimitAmount(entry.getValue());
            categoryBudgetRepository.save(cb);
        }
        return getBudget();
    }
}
