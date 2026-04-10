package com.aireviewer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReviewRequest {

    @JsonProperty("code")
    private String code;

    @JsonProperty("githubPrUrl")
    private String githubPrUrl;

    @JsonProperty("language")
    private String language;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getGithubPrUrl() { return githubPrUrl; }
    public void setGithubPrUrl(String githubPrUrl) { this.githubPrUrl = githubPrUrl; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}