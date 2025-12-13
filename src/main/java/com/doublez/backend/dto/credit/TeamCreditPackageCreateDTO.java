package com.doublez.backend.dto.credit;

import java.math.BigDecimal;

public class TeamCreditPackageCreateDTO {
    private String name;
    private String description;
    private Integer creditAmount;
    private BigDecimal price;
    private String currency = "RSD";
    private Boolean isActive = true;
    
    // TEAM-SPECIFIC FIELDS
    private Boolean isAgencyPackage = false;
    private Integer maxAgents; // null = unlimited
    private Integer maxSuperAgents; // null = unlimited
    private BigDecimal perAgentMonthlyCost; // Additional cost per agent/month
    private BigDecimal perSuperAgentMonthlyCost; // Additional cost per super agent/month
    private BigDecimal teamDiscountPercentage; // Discount when buying for team
    private Integer bonusCreditsForTeams; // Extra credits for team purchases
    private String features; // JSON array of features
    
    // getters and setters
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getCreditAmount() {
		return creditAmount;
	}
	public void setCreditAmount(Integer creditAmount) {
		this.creditAmount = creditAmount;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public Boolean getIsActive() {
		return isActive;
	}
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
	public Boolean getIsAgencyPackage() {
		return isAgencyPackage;
	}
	public void setIsAgencyPackage(Boolean isAgencyPackage) {
		this.isAgencyPackage = isAgencyPackage;
	}
	public Integer getMaxAgents() {
		return maxAgents;
	}
	public void setMaxAgents(Integer maxAgents) {
		this.maxAgents = maxAgents;
	}
	public Integer getMaxSuperAgents() {
		return maxSuperAgents;
	}
	public void setMaxSuperAgents(Integer maxSuperAgents) {
		this.maxSuperAgents = maxSuperAgents;
	}
	public BigDecimal getPerAgentMonthlyCost() {
		return perAgentMonthlyCost;
	}
	public void setPerAgentMonthlyCost(BigDecimal perAgentMonthlyCost) {
		this.perAgentMonthlyCost = perAgentMonthlyCost;
	}
	public BigDecimal getPerSuperAgentMonthlyCost() {
		return perSuperAgentMonthlyCost;
	}
	public void setPerSuperAgentMonthlyCost(BigDecimal perSuperAgentMonthlyCost) {
		this.perSuperAgentMonthlyCost = perSuperAgentMonthlyCost;
	}
	public BigDecimal getTeamDiscountPercentage() {
		return teamDiscountPercentage;
	}
	public void setTeamDiscountPercentage(BigDecimal teamDiscountPercentage) {
		this.teamDiscountPercentage = teamDiscountPercentage;
	}
	public Integer getBonusCreditsForTeams() {
		return bonusCreditsForTeams;
	}
	public void setBonusCreditsForTeams(Integer bonusCreditsForTeams) {
		this.bonusCreditsForTeams = bonusCreditsForTeams;
	}
	public String getFeatures() {
		return features;
	}
	public void setFeatures(String features) {
		this.features = features;
	}
    
}
