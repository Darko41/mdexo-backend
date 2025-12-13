package com.doublez.backend.entity.credit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import com.doublez.backend.enums.CreditPackageType;
import com.doublez.backend.enums.subscription.SubscriptionPeriod;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "credit_packages")
public class CreditPackage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(unique = true, nullable = false)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CreditPackageType type;

	@Column(name = "credit_amount", nullable = false)
	private Integer creditAmount;

	@Column(name = "price", precision = 10, scale = 2)
	private BigDecimal price; // null for free packages

	@Column(name = "is_active")
	private Boolean isActive = true;

	@Column(name = "duration_days")
	private Integer durationDays; // For subscriptions/features

	@Column(name = "recurring")
	private Boolean recurring = false; // For monthly subscriptions

	@Column(name = "features_included")
	private String featuresIncluded; // JSON description of features

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// TEAM-SPECIFIC FIELDS
	@Column(name = "is_agency_package")
	private Boolean isAgencyPackage = false;

	@Column(name = "max_agents")
	private Integer maxAgents; // Maximum agents for agency packages

	@Column(name = "max_super_agents")
	private Integer maxSuperAgents; // Maximum super agents for agency packages

	@Column(name = "per_agent_monthly_cost", precision = 10, scale = 2)
	private BigDecimal perAgentMonthlyCost; // Cost per additional agent/month

	@Column(name = "per_super_agent_monthly_cost", precision = 10, scale = 2)
	private BigDecimal perSuperAgentMonthlyCost; // Cost per super agent/month

	@Column(name = "team_discount_percentage", precision = 5, scale = 2)
	private BigDecimal teamDiscountPercentage; // Discount for team purchases

	@Column(name = "bonus_credits_for_teams")
	private Integer bonusCreditsForTeams; // Extra credits for team purchases

	@Column(name = "bulk_purchase_discount", precision = 5, scale = 2)
	private BigDecimal bulkPurchaseDiscount; // Discount for bulk credit purchases

	// ========================
	// CONSTRUCTORS
	// ========================

	public CreditPackage() {
	}

	public CreditPackage(String name, CreditPackageType type, Integer creditAmount, BigDecimal price) {
		this.name = name;
		this.type = type;
		this.creditAmount = creditAmount;
		this.price = price;
		this.isActive = true;
	}

	// ========================
	// LIFECYCLE METHODS
	// ========================

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	// ========================
	// BUSINESS METHODS
	// ========================

	public boolean isFree() {
		return price == null || price.compareTo(BigDecimal.ZERO) == 0;
	}

	public boolean isSubscription() {
		return type == CreditPackageType.TIER_SUBSCRIPTION && Boolean.TRUE.equals(recurring);
	}

	public boolean isOneTimePurchase() {
		return type == CreditPackageType.ONE_TIME || type == CreditPackageType.FEATURE;
	}

	public String getDisplayPrice() {
		if (isFree()) {
			return "FREE";
		}
		return "€" + price.toString();
	}

	public BigDecimal getPricePerCredit() {
		if (isFree() || creditAmount == 0) {
			return BigDecimal.ZERO;
		}
		return price.divide(BigDecimal.valueOf(creditAmount), 2, RoundingMode.HALF_UP);
	}

	/**
	 * Check if package is suitable for agency with given team size
	 */
	public boolean isSuitableForAgency(int agentCount, int superAgentCount) {
		if (!Boolean.TRUE.equals(isAgencyPackage)) {
			return false;
		}

		if (maxAgents != null && agentCount > maxAgents) {
			return false;
		}

		if (maxSuperAgents != null && superAgentCount > maxSuperAgents) {
			return false;
		}

		return true;
	}

	/**
	 * Calculate price for agency team
	 */
	public BigDecimal calculateTeamPrice(int numberOfAgents, int numberOfSuperAgents, SubscriptionPeriod period) {
		BigDecimal basePrice = getPrice() != null ? getPrice() : BigDecimal.ZERO;

		// Add agent costs (first agent is included in base price)
		BigDecimal agentCost = BigDecimal.ZERO;
		if (numberOfAgents > 1 && perAgentMonthlyCost != null) {
			int additionalAgents = Math.max(0, numberOfAgents - 1);
			agentCost = perAgentMonthlyCost.multiply(BigDecimal.valueOf(additionalAgents));
		}

		// Add super agent costs
		BigDecimal superAgentCost = BigDecimal.ZERO;
		if (numberOfSuperAgents > 0 && perSuperAgentMonthlyCost != null) {
			superAgentCost = perSuperAgentMonthlyCost.multiply(BigDecimal.valueOf(numberOfSuperAgents));
		}

		BigDecimal monthlyPrice = basePrice.add(agentCost).add(superAgentCost);

		// Apply team discount if applicable
		if (teamDiscountPercentage != null && teamDiscountPercentage.compareTo(BigDecimal.ZERO) > 0) {
			BigDecimal discount = monthlyPrice.multiply(teamDiscountPercentage).divide(BigDecimal.valueOf(100), 2,
					RoundingMode.HALF_UP);
			monthlyPrice = monthlyPrice.subtract(discount);
		}

		// Calculate for period
		return monthlyPrice.multiply(BigDecimal.valueOf(period.getMonths()));
	}

	/**
	 * Get total credits including team bonus
	 */
	public Integer getTotalCreditsWithTeamBonus() {
	    Integer total = creditAmount;
	    if (bonusCreditsForTeams != null) {
	        total += bonusCreditsForTeams;
	    }
	    return total;
	}

	/**
	 * Check if package allows bulk purchase discount
	 */
	public boolean hasBulkDiscount() {
	    return bulkPurchaseDiscount != null && bulkPurchaseDiscount.compareTo(BigDecimal.ZERO) > 0;
	}

	// ========================
	// GETTERS AND SETTERS
	// ========================

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public CreditPackageType getType() {
		return type;
	}

	public void setType(CreditPackageType type) {
		this.type = type;
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

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Integer getDurationDays() {
		return durationDays;
	}

	public void setDurationDays(Integer durationDays) {
		this.durationDays = durationDays;
	}

	public Boolean getRecurring() {
		return recurring;
	}

	public void setRecurring(Boolean recurring) {
		this.recurring = recurring;
	}

	public String getFeaturesIncluded() {
		return featuresIncluded;
	}

	public void setFeaturesIncluded(String featuresIncluded) {
		this.featuresIncluded = featuresIncluded;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
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

	public BigDecimal getBulkPurchaseDiscount() {
		return bulkPurchaseDiscount;
	}

	public void setBulkPurchaseDiscount(BigDecimal bulkPurchaseDiscount) {
		this.bulkPurchaseDiscount = bulkPurchaseDiscount;
	}

	@Override
	public String toString() {
		return "CreditPackage{" + "id=" + id + ", name='" + name + '\'' + ", creditAmount=" + creditAmount + ", price="
				+ price + ", type=" + type + '}';
	}
}
