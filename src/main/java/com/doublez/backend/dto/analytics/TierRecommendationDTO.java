package com.doublez.backend.dto.analytics;

public class TierRecommendationDTO {
    private String code;
    private String title;
    private String description;
    private String severity; // INFO, WARNING, CRITICAL
    
    // Default constructor
    public TierRecommendationDTO() {}
    
    // Parameterized constructor
    public TierRecommendationDTO(String code, String title, String description, String severity) {
        this.code = code;
        this.title = title;
        this.description = description;
        this.severity = severity;
    }
    
    // Getters and setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}
