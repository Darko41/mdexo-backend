package com.doublez.backend.dto.credit;

import java.util.List;

public class AgencyCreditBalanceDTO {
    private Long agencyId;
    private String agencyName;
    private Integer agencyPoolBalance; // Credits in agency pool
    private Integer totalTeamCredits; // Sum of all agent credits
    private Integer totalCombinedCredits; // Agency + Team
    private Boolean teamDistributionEnabled;
    private Integer distributionPercentage;
    private Integer totalAgents;
    private List<CreditTransactionResponseDTO> recentTransactions;
    
    // getters and setters
	public Long getAgencyId() {
		return agencyId;
	}
	public void setAgencyId(Long agencyId) {
		this.agencyId = agencyId;
	}
	public String getAgencyName() {
		return agencyName;
	}
	public void setAgencyName(String agencyName) {
		this.agencyName = agencyName;
	}
	public Integer getAgencyPoolBalance() {
		return agencyPoolBalance;
	}
	public void setAgencyPoolBalance(Integer agencyPoolBalance) {
		this.agencyPoolBalance = agencyPoolBalance;
	}
	public Integer getTotalTeamCredits() {
		return totalTeamCredits;
	}
	public void setTotalTeamCredits(Integer totalTeamCredits) {
		this.totalTeamCredits = totalTeamCredits;
	}
	public Integer getTotalCombinedCredits() {
		return totalCombinedCredits;
	}
	public void setTotalCombinedCredits(Integer totalCombinedCredits) {
		this.totalCombinedCredits = totalCombinedCredits;
	}
	public Boolean getTeamDistributionEnabled() {
		return teamDistributionEnabled;
	}
	public void setTeamDistributionEnabled(Boolean teamDistributionEnabled) {
		this.teamDistributionEnabled = teamDistributionEnabled;
	}
	public Integer getDistributionPercentage() {
		return distributionPercentage;
	}
	public void setDistributionPercentage(Integer distributionPercentage) {
		this.distributionPercentage = distributionPercentage;
	}
	public Integer getTotalAgents() {
		return totalAgents;
	}
	public void setTotalAgents(Integer totalAgents) {
		this.totalAgents = totalAgents;
	}
	public List<CreditTransactionResponseDTO> getRecentTransactions() {
		return recentTransactions;
	}
	public void setRecentTransactions(List<CreditTransactionResponseDTO> recentTransactions) {
		this.recentTransactions = recentTransactions;
	}
    
}
