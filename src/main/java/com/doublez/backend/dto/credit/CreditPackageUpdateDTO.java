package com.doublez.backend.dto.credit;

import java.math.BigDecimal;

import com.doublez.backend.enums.CreditPackageType;

public class CreditPackageUpdateDTO {
    private String name;
    private String description;
    private CreditPackageType type;
    private Integer creditAmount;
    private BigDecimal price;
    private Boolean isActive;
    private Integer durationDays;
    private Boolean recurring;
    private String featuresIncluded;

    // Constructors
    public CreditPackageUpdateDTO() {}

    // Helper methods
    public boolean hasUpdates() {
        return name != null || description != null || type != null || 
               creditAmount != null || price != null || isActive != null ||
               durationDays != null || recurring != null || featuresIncluded != null;
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
