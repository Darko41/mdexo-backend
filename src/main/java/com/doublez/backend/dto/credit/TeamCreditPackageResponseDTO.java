package com.doublez.backend.dto.credit;

import java.math.BigDecimal;

import com.doublez.backend.entity.credit.CreditPackage;
import com.doublez.backend.enums.subscription.SubscriptionPeriod;

public class TeamCreditPackageResponseDTO extends CreditPackageResponseDTO {
    
    // TEAM-SPECIFIC FIELDS
    private Boolean isAgencyPackage;
    private Integer maxAgents;
    private Integer maxSuperAgents;
    private BigDecimal perAgentMonthlyCost;
    private BigDecimal perSuperAgentMonthlyCost;
    private BigDecimal teamDiscountPercentage;
    private Integer bonusCreditsForTeams;
    private BigDecimal bulkPurchaseDiscount;
    private String[] featuresArray; // Parsed from featuresIncluded
    
    // CALCULATED FIELDS (for specific team)
    private BigDecimal calculatedMonthlyPrice;
    private BigDecimal calculatedTotalPrice;
    private Integer totalCreditsWithBonus;
    private Boolean suitableForCurrentTeam;
    
    // Constructor from CreditPackage entity
    public static TeamCreditPackageResponseDTO fromEntity(CreditPackage entity, 
                                                         Integer agentCount, 
                                                         Integer superAgentCount) {
        TeamCreditPackageResponseDTO dto = new TeamCreditPackageResponseDTO();
        
        // Copy base fields from parent
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setType(entity.getType());
        dto.setCreditAmount(entity.getCreditAmount());
        dto.setPrice(entity.getPrice());
        dto.setIsActive(entity.getIsActive());
        dto.setDurationDays(entity.getDurationDays());
        dto.setRecurring(entity.getRecurring());
        dto.setFeaturesIncluded(entity.getFeaturesIncluded());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        // Set team fields
        dto.setIsAgencyPackage(entity.getIsAgencyPackage());
        dto.setMaxAgents(entity.getMaxAgents());
        dto.setMaxSuperAgents(entity.getMaxSuperAgents());
        dto.setPerAgentMonthlyCost(entity.getPerAgentMonthlyCost());
        dto.setPerSuperAgentMonthlyCost(entity.getPerSuperAgentMonthlyCost());
        dto.setTeamDiscountPercentage(entity.getTeamDiscountPercentage());
        dto.setBonusCreditsForTeams(entity.getBonusCreditsForTeams());
        dto.setBulkPurchaseDiscount(entity.getBulkPurchaseDiscount());
        
        // Parse features if needed
        if (entity.getFeaturesIncluded() != null) {
            dto.setFeaturesArray(entity.getFeaturesIncluded().split(","));
        }
        
        // Calculate values if team size provided
        if (agentCount != null && superAgentCount != null) {
            dto.setSuitableForCurrentTeam(entity.isSuitableForAgency(agentCount, superAgentCount));
            dto.setTotalCreditsWithBonus(entity.getTotalCreditsWithTeamBonus());
            
            // Calculate prices for monthly subscription
            if (entity.getPrice() != null) {
                BigDecimal monthlyPrice = entity.calculateTeamPrice(
                    agentCount, superAgentCount, SubscriptionPeriod.MONTHLY);
                dto.setCalculatedMonthlyPrice(monthlyPrice);
                dto.setCalculatedTotalPrice(monthlyPrice);
            }
        }
        
        return dto;
    }
 
    
    // getters and setters

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

	public BigDecimal getCalculatedMonthlyPrice() {
		return calculatedMonthlyPrice;
	}

	public void setCalculatedMonthlyPrice(BigDecimal calculatedMonthlyPrice) {
		this.calculatedMonthlyPrice = calculatedMonthlyPrice;
	}

	public BigDecimal getCalculatedTotalPrice() {
		return calculatedTotalPrice;
	}

	public void setCalculatedTotalPrice(BigDecimal calculatedTotalPrice) {
		this.calculatedTotalPrice = calculatedTotalPrice;
	}


	public BigDecimal getBulkPurchaseDiscount() {
		return bulkPurchaseDiscount;
	}


	public void setBulkPurchaseDiscount(BigDecimal bulkPurchaseDiscount) {
		this.bulkPurchaseDiscount = bulkPurchaseDiscount;
	}


	public String[] getFeaturesArray() {
		return featuresArray;
	}


	public void setFeaturesArray(String[] featuresArray) {
		this.featuresArray = featuresArray;
	}


	public Integer getTotalCreditsWithBonus() {
		return totalCreditsWithBonus;
	}


	public void setTotalCreditsWithBonus(Integer totalCreditsWithBonus) {
		this.totalCreditsWithBonus = totalCreditsWithBonus;
	}


	public Boolean getSuitableForCurrentTeam() {
		return suitableForCurrentTeam;
	}


	public void setSuitableForCurrentTeam(Boolean suitableForCurrentTeam) {
		this.suitableForCurrentTeam = suitableForCurrentTeam;
	}
    
   
}
