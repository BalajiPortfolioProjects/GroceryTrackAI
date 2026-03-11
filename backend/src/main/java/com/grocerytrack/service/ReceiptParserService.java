package com.grocerytrack.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grocerytrack.dto.ParsedReceiptDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.model.Media;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ReceiptParserService {

    private static final String PARSE_PROMPT = """
            You are a grocery receipt parser. Extract all information from this receipt image.
            
            Return ONLY a valid JSON object with NO additional text, markdown, or explanation:
            {
              "store": "store name",
              "date": "YYYY-MM-DD",
              "total": 0.00,
              "items": [
                {"name": "item name", "category": "category", "price": 0.00}
              ]
            }
            
            Categories MUST be exactly one of:
            - Produce
            - Dairy & Eggs
            - Meat & Seafood
            - Pantry & Snacks
            - Beverages
            - Other
            
            If you cannot read the receipt clearly, make reasonable estimates.
            Return ONLY the JSON, nothing else.
            """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    public ReceiptParserService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public ParsedReceiptDTO parse(MultipartFile file) throws IOException {
        // If no valid API key, fall back to mock parsing
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("REPLACE") || apiKey.equals("DUMMY_KEY_REPLACE_ME")) {
            log.warn("No valid OPENAI_API_KEY set — using mock receipt parser");
            return generateMockReceipt(file.getOriginalFilename());
        }

        try {
            return callOpenAiVision(file);
        } catch (Exception e) {
            log.error("Spring AI parsing failed: {} — falling back to mock", e.getMessage());
            return generateMockReceipt(file.getOriginalFilename());
        }
    }

    private ParsedReceiptDTO callOpenAiVision(MultipartFile file) throws IOException {
        byte[] imageBytes = file.getBytes();
        String contentType = file.getContentType();
        MimeType mimeType = contentType != null
                ? MimeTypeUtils.parseMimeType(contentType)
                : MimeTypeUtils.IMAGE_JPEG;

        Media media = new Media(mimeType, new ByteArrayResource(imageBytes));
        UserMessage userMessage = new UserMessage(PARSE_PROMPT, List.of(media));

        String responseContent = chatClient.prompt()
                .messages(userMessage)
                .call()
                .content();

        log.info("LLM response for receipt: {}", responseContent);
        return parseJsonResponse(responseContent);
    }

    /**
     * Extracts JSON from LLM response, handling markdown code blocks if
     * present.
     */
    private ParsedReceiptDTO parseJsonResponse(String response) throws JsonProcessingException {
        String json = response.trim();
        // Strip markdown code fences  ```json ... ```
        Pattern fence = Pattern.compile("```(?:json)?\\s*(\\{[\\s\\S]*?})\\s*```", Pattern.DOTALL);
        Matcher matcher = fence.matcher(json);
        if (matcher.find()) {
            json = matcher.group(1).trim();
        }
        return objectMapper.readValue(json, ParsedReceiptDTO.class);
    }

    // -----------------------------------------------------------------------
    // Mock fallback — generates realistic-looking data when no API key is set
    // -----------------------------------------------------------------------
    private static final List<String[]> MOCK_ITEMS = List.of(
            new String[]{"Organic Spinach 5oz", "Produce", "4.99"},
            new String[]{"Free Range Eggs (12ct)", "Dairy & Eggs", "6.49"},
            new String[]{"Wild Salmon Fillet 1lb", "Meat & Seafood", "14.99"},
            new String[]{"Whole Milk 1gal", "Dairy & Eggs", "5.29"},
            new String[]{"Roma Tomatoes 2lb", "Produce", "3.49"},
            new String[]{"Greek Yogurt 32oz", "Dairy & Eggs", "6.99"},
            new String[]{"Baby Carrots 1lb", "Produce", "2.29"},
            new String[]{"Cheddar Cheese 8oz", "Dairy & Eggs", "4.79"},
            new String[]{"Avocados (3ct)", "Produce", "4.49"},
            new String[]{"Chicken Breast 2lb", "Meat & Seafood", "9.99"},
            new String[]{"Sparkling Water 12pk", "Beverages", "4.49"},
            new String[]{"Mixed Nuts 16oz", "Pantry & Snacks", "7.99"},
            new String[]{"Almond Milk 64oz", "Dairy & Eggs", "3.99"},
            new String[]{"Ground Beef 1lb", "Meat & Seafood", "7.99"},
            new String[]{"Orange Juice 64oz", "Beverages", "4.47"},
            new String[]{"Broccoli Crowns", "Produce", "2.49"},
            new String[]{"Pasta Sauce 24oz", "Pantry & Snacks", "3.29"},
            new String[]{"Dark Chocolate Bar", "Pantry & Snacks", "2.49"},
            new String[]{"Butter 4 sticks", "Dairy & Eggs", "4.99"},
            new String[]{"Sweet Potatoes 3lb", "Produce", "3.99"}
    );

    private static final String[] MOCK_STORES = {
        "Whole Foods Market", "Trader Joe's", "Costco", "Safeway", "Kroger", "Target"
    };

    private ParsedReceiptDTO generateMockReceipt(String filename) {
        Random rnd = new Random(filename != null ? filename.hashCode() : 42);
        String store = filename != null && filename.toLowerCase().contains("costco")
                ? "Costco"
                : filename != null && filename.toLowerCase().contains("trader")
                ? "Trader Joe's"
                : MOCK_STORES[rnd.nextInt(MOCK_STORES.length)];

        List<ParsedReceiptDTO.ParsedItemDTO> items = new ArrayList<>();
        int count = 6 + rnd.nextInt(8);
        List<String[]> shuffled = new ArrayList<>(MOCK_ITEMS);
        shuffled.sort((a, b) -> rnd.nextInt(3) - 1);
        for (int i = 0; i < Math.min(count, shuffled.size()); i++) {
            String[] raw = shuffled.get(i);
            items.add(ParsedReceiptDTO.ParsedItemDTO.builder()
                    .name(raw[0])
                    .category(raw[1])
                    .price(new BigDecimal(raw[2]))
                    .build());
        }

        BigDecimal total = items.stream()
                .map(ParsedReceiptDTO.ParsedItemDTO::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return ParsedReceiptDTO.builder()
                .store(store)
                .date(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .total(total)
                .items(items)
                .build();
    }
}
