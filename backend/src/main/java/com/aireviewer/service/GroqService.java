package com.aireviewer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GroqService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final String MODEL = "llama-3.3-70b-versatile";
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    @Value("${groq.api.key}")
    private String apiKey;

    public GroqService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public String reviewCode(String codeOrDiff, String language) {
        try {
            String systemPrompt = """
                    You are an expert AI code reviewer. Perform a thorough, structured code review.

                    Analyze the code across THREE dimensions:
                    1. 🐛 BUG ANALYSIS — logical errors, null pointer risks, edge cases, off-by-one errors
                    2. 🔒 SECURITY ANALYSIS — SQL injection, XSS, hardcoded secrets, missing input validation
                    3. ✨ STYLE & BEST PRACTICES — naming conventions, code duplication, complexity, readability

                    Format your response as markdown with these exact sections:
                    ## 🐛 Bug Analysis
                    ## 🔒 Security Analysis
                    ## ✨ Style & Best Practices
                    ## ✅ Overall Summary

                    In the summary, give a score out of 10 and list the top 3 most important things to fix.
                    Be specific: reference line numbers or code snippets where possible.
                    """;

            String userMessage = String.format(
                    "Please review the following %s code:\n\n```%s\n%s\n```",
                    language != null ? language : "code",
                    language != null ? language.toLowerCase() : "",
                    codeOrDiff
            );

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", MODEL);
            requestBody.put("max_tokens", 4096);
            requestBody.put("temperature", 0.3);

            ArrayNode messages = objectMapper.createArrayNode();

            ObjectNode systemMsg = objectMapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);

            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            requestBody.set("messages", messages);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Groq API returned status " + response.statusCode() + ": " + response.body());
            }

            JsonNode responseJson = objectMapper.readTree(response.body());
            return responseJson
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText("No review content returned.");

        } catch (Exception e) {
            throw new RuntimeException("Groq API error: " + e.getMessage(), e);
        }
    }
}