package com.grocerytrack.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * All AI configuration — bound from application.properties under the prefix
 * "app.ai".
 *
 * Override any field without touching Java code: app.ai.api-key=... (resolved
 * from env var in application.properties) app.ai.model=gemini-2.0-flash
 * app.ai.temperature=0.1 app.ai.max-tokens=1024 app.ai.prompt=...
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    /**
     * Resolved API key — populated from the appropriate environment variable
     * (GEMINI_API_KEY, GROQ_API_KEY, OPENAI_API_KEY, …) via
     * application.properties. The service uses this to decide whether to call
     * the real AI or fall back to mock parsing.
     */
    private String apiKey = "";

    /**
     * Model name — e.g. gemini-2.0-flash, llama-3.2-11b-vision-preview,
     * gpt-4o-mini.
     */
    private String model = "gemini-2.0-flash";

    /**
     * Sampling temperature — low values = more deterministic JSON output.
     */
    private double temperature = 0.1;

    /**
     * Maximum tokens to generate in the response.
     */
    private int maxTokens = 1024;

    /**
     * System prompt sent to the LLM when parsing a receipt image. Override in
     * application.properties as a multi-line value using the
     * backslash-continuation syntax.
     */
    private String prompt = """
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
}
