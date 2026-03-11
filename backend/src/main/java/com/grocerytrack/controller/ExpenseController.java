package com.grocerytrack.controller;

import com.grocerytrack.model.Receipt;
import com.grocerytrack.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ReceiptService receiptService;

    /**
     * GET /api/expenses Returns all receipts ordered by date descending.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllExpenses() {
        List<Receipt> receipts = receiptService.getAllReceipts();

        List<Map<String, Object>> data = receipts.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("store", r.getStore());
            m.put("date", r.getReceiptDate().toString());
            m.put("total", r.getTotal());
            m.put("itemsCount", r.getItemsCount());
            m.put("primaryCategory", r.getPrimaryCategory());
            m.put("categoryColor", r.getCategoryColor());
            m.put("categoryBg", r.getCategoryBg());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("expenses", data);
        response.put("count", data.size());
        response.put("totalSpent", receipts.stream()
                .map(Receipt::getTotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));

        return ResponseEntity.ok(response);
    }
}
