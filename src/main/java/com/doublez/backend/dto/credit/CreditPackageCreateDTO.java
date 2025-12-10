package com.doublez.backend.dto.credit;

import java.math.BigDecimal;

import com.doublez.backend.enums.CreditPackageType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreditPackageCreateDTO {
    @NotBlank 
    private String name;
    
    private String description;
    
    @NotNull
    private CreditPackageType type;
    
    @NotNull @Min(1)
    private Integer creditAmount;
    
    @DecimalMin("0.00")
    private BigDecimal price;
    
    private Boolean isActive = true;
    private Integer durationDays;
    private Boolean recurring = false;
    private String featuresIncluded;

    // Constructors
    public CreditPackageCreateDTO() {}

    // Helper methods
    public boolean isFree() {
        return price == null || price.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isSubscription() {
        return type == CreditPackageType.TIER_SUBSCRIPTION && Boolean.TRUE.equals(recurring);
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public CreditPackageType getType() { return type; }
    public void setType(CreditPackageType type) { this.type = type; }
    public Integer getCreditAmount() { return creditAmount; }
    public void setCreditAmount(Integer creditAmount) { this.creditAmount = creditAmount; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    public Boolean getRecurring() { return recurring; }
    public void setRecurring(Boolean recurring) { this.recurring = recurring; }
    public String getFeaturesIncluded() { return featuresIncluded; }
    public void setFeaturesIncluded(String featuresIncluded) { this.featuresIncluded = featuresIncluded; }
}