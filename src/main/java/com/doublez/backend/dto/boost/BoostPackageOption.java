package com.doublez.backend.dto.boost;

import com.doublez.backend.config.CreditPricingConfiguration;

public class BoostPackageOption {
	private String packageType;
	private String displayName;
	private int creditCost;
	private String description;
	private boolean canAfford;
	private int rsdCost;
	private int individualValue; // Total cost if bought separately
	private int savings; // How much they save

	public BoostPackageOption(String packageType, String displayName, int creditCost, String description,
			int userCredits) {
		this.packageType = packageType;
		this.displayName = displayName;
		this.creditCost = creditCost;
		this.description = description;
		this.canAfford = userCredits >= creditCost;
		this.rsdCost = creditCost; // 1:1 with RSD

		// Calculate savings
		this.individualValue = calculateIndividualValue(packageType);
		this.savings = this.individualValue - this.creditCost;
	}

	private int calculateIndividualValue(String packageType) {
	    return switch (packageType) {
	        case "BRONZE" -> CreditPricingConfiguration.TOP_POSITIONING_BOOST_7DAYS
	                        + CreditPricingConfiguration.URGENT_BADGE_14DAYS;
	                        
	        case "SILVER" -> CreditPricingConfiguration.TOP_POSITIONING_BOOST_7DAYS
	                        + CreditPricingConfiguration.URGENT_BADGE_14DAYS
	                        + CreditPricingConfiguration.HIGHLIGHTED_LISTING_30DAYS
	                        + CreditPricingConfiguration.FEATURED_IN_CATEGORY_15DAYS;
	                        
	        case "GOLD" -> CreditPricingConfiguration.TOP_POSITIONING_BOOST_7DAYS
	                      + CreditPricingConfiguration.URGENT_BADGE_14DAYS
	                      + CreditPricingConfiguration.HIGHLIGHTED_LISTING_30DAYS
	                      + CreditPricingConfiguration.FEATURED_IN_CATEGORY_15DAYS
	                      + CreditPricingConfiguration.VERIFIED_BADGE_30DAYS
	                      + CreditPricingConfiguration.PREMIUM_PROFILE_BADGE_30DAYS;
	                      
	        default -> creditCost;
	    };
	}

	// Getters and setters
	public String getPackageType() {
		return packageType;
	}

	public void setPackageType(String packageType) {
		this.packageType = packageType;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getCreditCost() {
		return creditCost;
	}

	public void setCreditCost(int creditCost) {
		this.creditCost = creditCost;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isCanAfford() {
		return canAfford;
	}

	public void setCanAfford(boolean canAfford) {
		this.canAfford = canAfford;
	}

	public int getRsdCost() {
		return rsdCost;
	}

	public void setRsdCost(int rsdCost) {
		this.rsdCost = rsdCost;
	}

	public int getIndividualValue() {
		return individualValue;
	}

	public void setIndividualValue(int individualValue) {
		this.individualValue = individualValue;
	}

	public int getSavings() {
		return savings;
	}

	public void setSavings(int savings) {
		this.savings = savings;
	}
}