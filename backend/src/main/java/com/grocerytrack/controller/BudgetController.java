package com.grocerytrack.controller;

import com.grocerytrack.dto.BudgetDTO;
import com.grocerytrack.service.BudgetService;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * GET /api/budget
     */
    @GetMapping
    public ResponseEntity<BudgetDTO> getBudget() {
        return ResponseEntity.ok(budgetService.getBudget());
    }

    /**
     * PUT /api/budget Body: { "weeklyAmount": 250.00 }
     */
    @PutMapping
    public ResponseEntity<BudgetDTO> setWeeklyBudget(@RequestBody Map<String, Object> body) {
        Object amtObj = body.get("weeklyAmount");
        if (amtObj == null) {
            return ResponseEntity.badRequest().build();
        }
        BigDecimal amount = new BigDecimal(amtObj.toString());
        return ResponseEntity.ok(budgetService.setWeeklyBudget(amount));
    }

    /**
     * PUT /api/budget/categories Body: { "Produce": 60.00, "Dairy & Eggs":
     * 44.00, ... }
     */
    @PutMapping("/categories")
    public ResponseEntity<BudgetDTO> setCategoryBudgets(@RequestBody Map<String, Object> body) {
        Map<String, BigDecimal> categoryLimits = new java.util.HashMap<>();
        body.forEach((k, v) -> categoryLimits.put(k, new BigDecimal(v.toString())));
        return ResponseEntity.ok(budgetService.setCategoryBudgets(categoryLimits));
    }
}
