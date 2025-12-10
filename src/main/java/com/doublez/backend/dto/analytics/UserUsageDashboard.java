package com.doublez.backend.dto.analytics;

import java.util.ArrayList;
import java.util.List;

import com.doublez.backend.dto.credit.CreditTransactionResponseDTO;
import com.doublez.backend.dto.user.UsageStatsDTO;
import com.doublez.backend.entity.user.UserTier;
import com.doublez.backend.upgrade.UpgradeSuggestion;

public class UserUsageDashboard {
    private Long userId;
    private UserTier userTier;
    private Integer currentCredits;
    private UsageStatsDTO simpleStats;           // Your simple DTO for quick overview
    private AgencyUsageStats agencyUsageStats;   // Detailed agency analytics
    private List<CreditTransactionResponseDTO> recentTransactions;
    private List<UpgradeSuggestion> upgradeSuggestions;
    
    public UserUsageDashboard() {
        this.recentTransactions = new ArrayList<>();
        this.upgradeSuggestions = new ArrayList<>();
    }
    
    // Getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public UserTier getUserTier() { return userTier; }
    public void setUserTier(UserTier userTier) { this.userTier = userTier; }
    
    public Integer getCurrentCredits() { return currentCredits; }
    public void setCurrentCredits(Integer currentCredits) { this.currentCredits = currentCredits; }
    
    public UsageStatsDTO getSimpleStats() { return simpleStats; }
    public void setSimpleStats(UsageStatsDTO simpleStats) { this.simpleStats = simpleStats; }
    
    public AgencyUsageStats getAgencyUsageStats() { return agencyUsageStats; }
    public void setAgencyUsageStats(AgencyUsageStats agencyUsageStats) { this.agencyUsageStats = agencyUsageStats; }
    
    public List<CreditTransactionResponseDTO> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<CreditTransactionResponseDTO> recentTransactions) { 
        this.recentTransactions = recentTransactions; 
    }
    
    public List<UpgradeSuggestion> getUpgradeSuggestions() { return upgradeSuggestions; }
    public void setUpgradeSuggestions(List<UpgradeSuggestion> upgradeSuggestions) { 
        this.upgradeSuggestions = upgradeSuggestions; 
    }
}
