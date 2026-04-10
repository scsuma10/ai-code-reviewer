package com.aireviewer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GitHubService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${github.token:}")
    private String githubToken;

    public GitHubService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public GitHubPRData fetchPRData(String prUrl) {
        Pattern pattern = Pattern.compile("github\\.com/([^/]+)/([^/]+)/pull/(\\d+)");
        Matcher matcher = pattern.matcher(prUrl);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid GitHub PR URL: " + prUrl);
        }

        String owner = matcher.group(1);
        String repo = matcher.group(2);
        String prNumber = matcher.group(3);

        try {
            // Fetch PR metadata
            String metaJson = get("https://api.github.com/repos/" + owner + "/" + repo + "/pulls/" + prNumber);
            // Fetch PR files
            String filesJson = get("https://api.github.com/repos/" + owner + "/" + repo + "/pulls/" + prNumber + "/files");

            JsonNode meta = objectMapper.readTree(metaJson);
            JsonNode files = objectMapper.readTree(filesJson);

            String title = meta.path("title").asText("Unknown PR");
            String description = meta.path("body").asText("");

            StringBuilder diffBuilder = new StringBuilder();
            diffBuilder.append("PR Title: ").append(title).append("\n");
            diffBuilder.append("Description: ").append(description).append("\n\n");
            diffBuilder.append("=== Changed Files ===\n\n");

            for (JsonNode file : files) {
                String filename = file.path("filename").asText();
                String status = file.path("status").asText();
                String patch = file.path("patch").asText("");
                diffBuilder.append("File: ").append(filename)
                        .append(" [").append(status).append("]\n");
                diffBuilder.append(patch).append("\n\n");
            }

            return new GitHubPRData(title, diffBuilder.toString());

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch GitHub PR: " + e.getMessage(), e);
        }
    }

    private String get(String url) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github.v3+json");

        if (githubToken != null && !githubToken.isBlank()) {
            builder.header("Authorization", "Bearer " + githubToken);
        }

        HttpResponse<String> response = httpClient.send(builder.GET().build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("GitHub API error " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }

    public static class GitHubPRData {
        public final String title;
        public final String diff;

        public GitHubPRData(String title, String diff) {
            this.title = title;
            this.diff = diff;
        }
    }
}