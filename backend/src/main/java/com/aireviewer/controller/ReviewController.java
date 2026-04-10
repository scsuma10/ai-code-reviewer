package com.aireviewer.controller;
import com.aireviewer.model.ReviewRequest;
import com.aireviewer.model.ReviewResponse;
import com.aireviewer.service.GroqService;
import com.aireviewer.service.GitHubService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ReviewController {

    private final GroqService groqService;
    private final GitHubService gitHubService;

    public ReviewController(GroqService groqService, GitHubService gitHubService) {
        this.groqService = groqService;
        this.gitHubService = gitHubService;
    }

    /**
     * POST /api/review
     * Accepts either pasted code or a GitHub PR URL (or both).
     */
    @PostMapping("/review")
    public ResponseEntity<ReviewResponse> review(@RequestBody ReviewRequest request) {
        try {
            String codeToReview;
            String source;
            String prTitle = null;

            boolean hasPrUrl = request.getGithubPrUrl() != null && !request.getGithubPrUrl().isBlank();
            boolean hasCode = request.getCode() != null && !request.getCode().isBlank();

            if (hasPrUrl) {
                // Fetch PR diff from GitHub
                GitHubService.GitHubPRData prData = gitHubService.fetchPRData(request.getGithubPrUrl());
                codeToReview = prData.diff;
                prTitle = prData.title;
                source = "github";

                // Append manually pasted code if also provided
                if (hasCode) {
                    codeToReview += "\n\n=== Additional Code Provided ===\n" + request.getCode();
                }
            } else if (hasCode) {
                codeToReview = request.getCode();
                source = "paste";
            } else {
                return ResponseEntity.badRequest().build();
            }

            String review = groqService.reviewCode(codeToReview, request.getLanguage());
            return ResponseEntity.ok(new ReviewResponse(review, source, prTitle));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI Code Reviewer is running!");
    }
}