package com.doublez.backend.dto.credit;

import java.util.ArrayList;
import java.util.List;

public class TeamCreditSummaryDTO {
    private Long agencyId;
    private String agencyName;
    private Integer totalAgents;
    private Integer totalTeamCredits;
    private Integer agentsWithCredits;
    private Integer averageCreditsPerAgent;
    
    // CHANGE THIS: From CreditTransaction to CreditTransactionResponseDTO
    private List<CreditTransactionResponseDTO> recentTransactions;

    // Constructors
    public TeamCreditSummaryDTO() {
        this.recentTransactions = new ArrayList<>();
    }

    // Getters and Setters
    public Long getAgencyId() { return agencyId; }
    public void setAgencyId(Long agencyId) { this.agencyId = agencyId; }
    
    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }
    
    public Integer getTotalAgents() { return totalAgents; }
    public void setTotalAgents(Integer totalAgents) { this.totalAgents = totalAgents; }
    
    public Integer getTotalTeamCredits() { return totalTeamCredits; }
    public void setTotalTeamCredits(Integer totalTeamCredits) { this.totalTeamCredits = totalTeamCredits; }
    
    public Integer getAgentsWithCredits() { return agentsWithCredits; }
    public void setAgentsWithCredits(Integer agentsWithCredits) { this.agentsWithCredits = agentsWithCredits; }
    
    public Integer getAverageCreditsPerAgent() { return averageCreditsPerAgent; }
    public void setAverageCreditsPerAgent(Integer averageCreditsPerAgent) { this.averageCreditsPerAgent = averageCreditsPerAgent; }
    
    // FIXED: Change type to CreditTransactionResponseDTO
    public List<CreditTransactionResponseDTO> getRecentTransactions() { 
        return recentTransactions; 
    }
    
    // FIXED: Change type to CreditTransactionResponseDTO
    public void setRecentTransactions(List<CreditTransactionResponseDTO> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    // Helper methods
    public boolean hasTeamCredits() {
        return totalTeamCredits != null && totalTeamCredits > 0;
    }
    
    public double getPercentageWithCredits() {
        if (totalAgents == null || totalAgents == 0) return 0.0;
        return (agentsWithCredits * 100.0) / totalAgents;
    }
    
    public String getCreditsDistributionInfo() {
        if (totalAgents == null || totalAgents == 0) return "No agents in team";
        
        return String.format("%d/%d agents have credits (%.1f%%)", 
            agentsWithCredits, totalAgents, getPercentageWithCredits());
    }

    @Override
    public String toString() {
        return "TeamCreditSummaryDTO{" +
                "agencyId=" + agencyId +
                ", agencyName='" + agencyName + '\'' +
                ", totalAgents=" + totalAgents +
                ", totalTeamCredits=" + totalTeamCredits +
                ", agentsWithCredits=" + agentsWithCredits +
                '}';
    }
}