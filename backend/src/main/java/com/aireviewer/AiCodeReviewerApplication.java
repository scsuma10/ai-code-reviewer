package com.aireviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.aireviewer")
public class AiCodeReviewerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiCodeReviewerApplication.class, args);
    }
}