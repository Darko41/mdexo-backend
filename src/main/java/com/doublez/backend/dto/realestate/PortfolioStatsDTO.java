package com.doublez.backend.dto.realestate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import com.doublez.backend.enums.ListingType;
import com.doublez.backend.enums.property.PropertyType;

public class PortfolioStatsDTO {
    private Long totalProperties;
    private Long activeProperties;
    private Long inactiveProperties;
    private BigDecimal totalPortfolioValue;
    private BigDecimal averagePropertyValue;
    private Map<PropertyType, Long> propertiesByType;
    private Map<String, Long> propertiesByCity;
    private Map<ListingType, Long> propertiesByListingType;
    private String portfolioHealth;
    private Long totalViews;
    private Long totalContacts;
    
    // Constructor
    public PortfolioStatsDTO(Long totalProperties, Long activeProperties, BigDecimal totalPortfolioValue,
                           Map<PropertyType, Long> propertiesByType, Map<String, Long> propertiesByCity,
                           Map<ListingType, Long> propertiesByListingType, String portfolioHealth,
                           Long totalViews, Long totalContacts) {
        this.totalProperties = totalProperties;
        this.activeProperties = activeProperties;
        this.inactiveProperties = totalProperties - activeProperties;
        this.totalPortfolioValue = totalPortfolioValue;
        this.averagePropertyValue = totalProperties > 0 ? 
            totalPortfolioValue.divide(BigDecimal.valueOf(totalProperties), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.propertiesByType = propertiesByType;
        this.propertiesByCity = propertiesByCity;
        this.propertiesByListingType = propertiesByListingType;
        this.portfolioHealth = portfolioHealth;
        this.totalViews = totalViews;
        this.totalContacts = totalContacts;
    }

	public Long getTotalProperties() {
		return totalProperties;
	}

	public void setTotalProperties(Long totalProperties) {
		this.totalProperties = totalProperties;
	}

	public Long getActiveProperties() {
		return activeProperties;
	}

	public void setActiveProperties(Long activeProperties) {
		this.activeProperties = activeProperties;
	}

	public Long getInactiveProperties() {
		return inactiveProperties;
	}

	public void setInactiveProperties(Long inactiveProperties) {
		this.inactiveProperties = inactiveProperties;
	}

	public BigDecimal getTotalPortfolioValue() {
		return totalPortfolioValue;
	}

	public void setTotalPortfolioValue(BigDecimal totalPortfolioValue) {
		this.totalPortfolioValue = totalPortfolioValue;
	}

	public BigDecimal getAveragePropertyValue() {
		return averagePropertyValue;
	}

	public void setAveragePropertyValue(BigDecimal averagePropertyValue) {
		this.averagePropertyValue = averagePropertyValue;
	}

	public Map<PropertyType, Long> getPropertiesByType() {
		return propertiesByType;
	}

	public void setPropertiesByType(Map<PropertyType, Long> propertiesByType) {
		this.propertiesByType = propertiesByType;
	}

	public Map<String, Long> getPropertiesByCity() {
		return propertiesByCity;
	}

	public void setPropertiesByCity(Map<String, Long> propertiesByCity) {
		this.propertiesByCity = propertiesByCity;
	}

	public Map<ListingType, Long> getPropertiesByListingType() {
		return propertiesByListingType;
	}

	public void setPropertiesByListingType(Map<ListingType, Long> propertiesByListingType) {
		this.propertiesByListingType = propertiesByListingType;
	}

	public String getPortfolioHealth() {
		return portfolioHealth;
	}

	public void setPortfolioHealth(String portfolioHealth) {
		this.portfolioHealth = portfolioHealth;
	}

	public Long getTotalViews() {
		return totalViews;
	}

	public void setTotalViews(Long totalViews) {
		this.totalViews = totalViews;
	}

	public Long getTotalContacts() {
		return totalContacts;
	}

	public void setTotalContacts(Long totalContacts) {
		this.totalContacts = totalContacts;
	}
    
    
}
