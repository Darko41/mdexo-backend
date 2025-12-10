package com.doublez.backend.upgrade;

import com.doublez.backend.entity.user.UserTier;

public class UpgradeSuggestionBuilder {
    private String type;
    private String message;
    private UserTier suggestedTier;
    private String priority;
    
    public UpgradeSuggestionBuilder type(String type) {
        this.type = type;
        return this;
    }
    
    public UpgradeSuggestionBuilder message(String message) {
        this.message = message;
        return this;
    }
    
    public UpgradeSuggestionBuilder suggestedTier(UserTier suggestedTier) {
        this.suggestedTier = suggestedTier;
        return this;
    }
    
    public UpgradeSuggestionBuilder priority(String priority) {
        this.priority = priority;
        return this;
    }
    
    public UpgradeSuggestion build() {
        return new UpgradeSuggestion(type, message, suggestedTier, priority);
    }
}