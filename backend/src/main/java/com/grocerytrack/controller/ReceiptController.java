package com.grocerytrack.controller;

import com.grocerytrack.model.Receipt;
import com.grocerytrack.model.ReceiptItem;
import com.grocerytrack.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    /**
     * POST /api/receipts/upload Accepts multipart file, parses with Spring AI,
     * saves to DB.
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadReceipt(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        try {
            Receipt saved = receiptService.uploadAndParse(file);
            return ResponseEntity.ok(toDetailMap(saved));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to process file: " + e.getMessage()));
        }
    }

    /**
     * GET /api/receipts/{id} Returns a single receipt with all its items.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReceipt(@PathVariable Long id) {
        Receipt receipt = receiptService.getById(id);
        return ResponseEntity.ok(toDetailMap(receipt));
    }

    private Map<String, Object> toDetailMap(Receipt r) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", r.getId());
        map.put("store", r.getStore());
        map.put("date", r.getReceiptDate().toString());
        map.put("total", r.getTotal());
        map.put("itemsCount", r.getItemsCount());
        map.put("primaryCategory", r.getPrimaryCategory());
        map.put("categoryColor", r.getCategoryColor());
        map.put("categoryBg", r.getCategoryBg());
        List<Map<String, Object>> items = r.getItems().stream().map(i -> {
            Map<String, Object> im = new HashMap<>();
            im.put("id", i.getId());
            im.put("name", i.getName());
            im.put("category", i.getCategory());
            im.put("categoryColor", i.getCategoryColor());
            im.put("categoryBg", i.getCategoryBg());
            im.put("price", i.getPrice());
            return im;
        }).collect(Collectors.toList());
        map.put("items", items);
        return map;
    }
}
