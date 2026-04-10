package com.aireviewer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReviewResponse {

    @JsonProperty("review")
    private String review;

    @JsonProperty("source")
    private String source;

    @JsonProperty("prTitle")
    private String prTitle;

    public ReviewResponse() {}

    public ReviewResponse(String review, String source, String prTitle) {
        this.review = review;
        this.source = source;
        this.prTitle = prTitle;
    }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getPrTitle() { return prTitle; }
    public void setPrTitle(String prTitle) { this.prTitle = prTitle; }
}