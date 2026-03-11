package com.grocerytrack.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grocerytrack.config.AiProperties;
import com.grocerytrack.dto.ParsedReceiptDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.model.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

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
@RequiredArgsConstructor
public class ReceiptParserService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;
    private final AiProperties aiProperties;

    public ParsedReceiptDTO parse(MultipartFile file) throws IOException {
        String key = aiProperties.getApiKey();
        if (key == null || key.isBlank() || key.startsWith("REPLACE")) {
            log.warn("No AI API key configured (app.ai.api-key) — using mock receipt parser");
            return generateMockReceipt(file.getOriginalFilename());
        }
        try {
            return callAiVision(file);
        } catch (Exception e) {
            log.error("AI parsing failed: {} — falling back to mock", e.getMessage());
            return generateMockReceipt(file.getOriginalFilename());
        }
    }

    private ParsedReceiptDTO callAiVision(MultipartFile file) throws IOException {
        byte[] imageBytes = file.getBytes();
        String contentType = file.getContentType();
        MimeType mimeType = contentType != null
                ? MimeTypeUtils.parseMimeType(contentType)
                : MimeTypeUtils.IMAGE_JPEG;

        // Model, temperature and max-tokens are configured via Spring AI property binding:
        //   spring.ai.openai.chat.options.model
        //   spring.ai.openai.chat.options.temperature
        //   spring.ai.openai.chat.options.max-tokens
        // No provider-specific options class needed here.
        Media media = new Media(mimeType, new ByteArrayResource(imageBytes));
        UserMessage userMessage = new UserMessage(aiProperties.getPrompt(), List.of(media));

        log.info("Calling AI model={}", aiProperties.getModel());

        String responseContent = chatClientBuilder.build()
                .prompt()
                .messages(userMessage)
                .call()
                .content();

        log.info("LLM response for receipt: {}", responseContent);
        return parseJsonResponse(responseContent);
    }

    private ParsedReceiptDTO parseJsonResponse(String response) throws JsonProcessingException {
        String json = response.trim();
        Pattern fence = Pattern.compile("```(?:json)?\\s*(\\{[\\s\\S]*?})\\s*```", Pattern.DOTALL);
        Matcher matcher = fence.matcher(json);
        if (matcher.find()) {
            json = matcher.group(1).trim();
        }
        return objectMapper.readValue(json, ParsedReceiptDTO.class);
    }

    // ── Mock fallback ──────────────────────────────────────────────────────
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
                    .name(raw[0]).category(raw[1]).price(new BigDecimal(raw[2])).build());
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
