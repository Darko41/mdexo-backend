package com.doublez.backend.dto.credit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.doublez.backend.enums.CreditPackageType;

public class CreditPackageResponseDTO {
    private Long id;
    private String name;
    private String description;
    private CreditPackageType type;
    private Integer creditAmount;
    private BigDecimal price;
    private Boolean isActive;
    private Integer durationDays;
    private Boolean recurring;
    private String featuresIncluded;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private Boolean isFree;
    private Boolean isSubscription;
    private String displayPrice;
    private BigDecimal pricePerCredit;

    // Constructors
    public CreditPackageResponseDTO() {}

    // Helper methods
    public boolean isOneTimePurchase() {
        return type == CreditPackageType.ONE_TIME || type == CreditPackageType.FEATURE;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Boolean getIsFree() { return isFree; }
    public void setIsFree(Boolean isFree) { this.isFree = isFree; }
    public Boolean getIsSubscription() { return isSubscription; }
    public void setIsSubscription(Boolean isSubscription) { this.isSubscription = isSubscription; }
    public String getDisplayPrice() { return displayPrice; }
    public void setDisplayPrice(String displayPrice) { this.displayPrice = displayPrice; }
    public BigDecimal getPricePerCredit() { return pricePerCredit; }
    public void setPricePerCredit(BigDecimal pricePerCredit) { this.pricePerCredit = pricePerCredit; }
}
