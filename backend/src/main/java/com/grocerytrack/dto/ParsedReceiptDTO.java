package com.grocerytrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedReceiptDTO {

    private String store;
    private String date;
    private BigDecimal total;
    private List<ParsedItemDTO> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParsedItemDTO {

        private String name;
        private String category;
        private BigDecimal price;
    }
}
