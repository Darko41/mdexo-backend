package com.doublez.backend.upgrade;

import com.doublez.backend.entity.user.UserTier;

public class UpgradeSuggestion {
    private String type;
    private String message;
    private UserTier suggestedTier;
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    
    public UpgradeSuggestion() {}
    
    public UpgradeSuggestion(String type, String message, UserTier suggestedTier, String priority) {
        this.type = type;
        this.message = message;
        this.suggestedTier = suggestedTier;
        this.priority = priority;
    }
    
    // Builder pattern
    public static UpgradeSuggestionBuilder builder() {
        return new UpgradeSuggestionBuilder();
    }
    
    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public UserTier getSuggestedTier() { return suggestedTier; }
    public void setSuggestedTier(UserTier suggestedTier) { this.suggestedTier = suggestedTier; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
