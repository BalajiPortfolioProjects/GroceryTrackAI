package com.grocerytrack.service;

import com.grocerytrack.dto.ParsedReceiptDTO;
import com.grocerytrack.model.Receipt;
import com.grocerytrack.model.ReceiptItem;
import com.grocerytrack.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptParserService receiptParserService;

    // Category → (color, bg)
    private static final Map<String, String[]> CAT_COLORS = Map.of(
            "Produce", new String[]{"#16A34A", "#F0FDF4"},
            "Dairy & Eggs", new String[]{"#2563EB", "#EFF6FF"},
            "Meat & Seafood", new String[]{"#DC2626", "#FEF2F2"},
            "Pantry & Snacks", new String[]{"#F59E0B", "#FFFBEB"},
            "Beverages", new String[]{"#8B5CF6", "#F5F3FF"},
            "Other", new String[]{"#6B7280", "#F3F4F6"}
    );

    @Transactional
    public Receipt uploadAndParse(MultipartFile file) throws IOException {
        ParsedReceiptDTO parsed = receiptParserService.parse(file);

        Receipt receipt = new Receipt();
        receipt.setStore(parsed.getStore());
        receipt.setTotal(parsed.getTotal() != null ? parsed.getTotal() : BigDecimal.ZERO);
        receipt.setCreatedAt(LocalDateTime.now());

        // Parse date
        LocalDate receiptDate = LocalDate.now();
        if (parsed.getDate() != null) {
            try {
                receiptDate = LocalDate.parse(parsed.getDate());
            } catch (DateTimeParseException e) {
                log.warn("Could not parse date '{}', using today", parsed.getDate());
            }
        }
        receipt.setReceiptDate(receiptDate);

        // Detect primary category from first item
        String primaryCat = "Other";
        if (parsed.getItems() != null && !parsed.getItems().isEmpty()) {
            primaryCat = parsed.getItems().get(0).getCategory();
            if (!CAT_COLORS.containsKey(primaryCat)) {
                primaryCat = "Other";
            }
        }
        String[] colors = CAT_COLORS.getOrDefault(primaryCat, CAT_COLORS.get("Other"));
        receipt.setPrimaryCategory(primaryCat);
        receipt.setCategoryColor(colors[0]);
        receipt.setCategoryBg(colors[1]);

        // Build items
        int itemCount = 0;
        if (parsed.getItems() != null) {
            for (ParsedReceiptDTO.ParsedItemDTO parsedItem : parsed.getItems()) {
                ReceiptItem item = new ReceiptItem();
                item.setName(parsedItem.getName());
                item.setCategory(parsedItem.getCategory() != null ? parsedItem.getCategory() : "Other");
                item.setPrice(parsedItem.getPrice() != null ? parsedItem.getPrice() : BigDecimal.ZERO);
                String[] c = CAT_COLORS.getOrDefault(item.getCategory(), CAT_COLORS.get("Other"));
                item.setCategoryColor(c[0]);
                item.setCategoryBg(c[1]);
                receipt.addItem(item);
                itemCount++;
            }
        }
        receipt.setItemsCount(itemCount);

        return receiptRepository.save(receipt);
    }

    public List<Receipt> getAllReceipts() {
        return receiptRepository.findAllByOrderByReceiptDateDescCreatedAtDesc();
    }

    public Receipt getById(Long id) {
        return receiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found: " + id));
    }
}
