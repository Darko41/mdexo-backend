package com.doublez.backend.dto.credit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.doublez.backend.enums.subscription.SubscriptionPeriod;

public class AgencyPurchaseQuoteDTO {
    private Long agencyId;
    private Long creditPackageId;
    private String packageName;
    private Integer numberOfAgents;
    private Integer numberOfSuperAgents;
    private SubscriptionPeriod period;
    private BigDecimal basePrice;
    private BigDecimal discountAmount;
    private BigDecimal finalPrice;
    private Integer totalCredits;
    private BigDecimal pricePerCredit;
    private LocalDateTime validUntil;
    private LocalDateTime calculatedAt = LocalDateTime.now();
    
    // getters and setters
	public Long getAgencyId() {
		return agencyId;
	}
	public void setAgencyId(Long agencyId) {
		this.agencyId = agencyId;
	}
	public Long getCreditPackageId() {
		return creditPackageId;
	}
	public void setCreditPackageId(Long creditPackageId) {
		this.creditPackageId = creditPackageId;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public Integer getNumberOfAgents() {
		return numberOfAgents;
	}
	public void setNumberOfAgents(Integer numberOfAgents) {
		this.numberOfAgents = numberOfAgents;
	}
	public Integer getNumberOfSuperAgents() {
		return numberOfSuperAgents;
	}
	public void setNumberOfSuperAgents(Integer numberOfSuperAgents) {
		this.numberOfSuperAgents = numberOfSuperAgents;
	}
	public SubscriptionPeriod getPeriod() {
		return period;
	}
	public void setPeriod(SubscriptionPeriod period) {
		this.period = period;
	}
	public BigDecimal getBasePrice() {
		return basePrice;
	}
	public void setBasePrice(BigDecimal basePrice) {
		this.basePrice = basePrice;
	}
	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}
	public void setDiscountAmount(BigDecimal discountAmount) {
		this.discountAmount = discountAmount;
	}
	public BigDecimal getFinalPrice() {
		return finalPrice;
	}
	public void setFinalPrice(BigDecimal finalPrice) {
		this.finalPrice = finalPrice;
	}
	public Integer getTotalCredits() {
		return totalCredits;
	}
	public void setTotalCredits(Integer totalCredits) {
		this.totalCredits = totalCredits;
	}
	public BigDecimal getPricePerCredit() {
		return pricePerCredit;
	}
	public void setPricePerCredit(BigDecimal pricePerCredit) {
		this.pricePerCredit = pricePerCredit;
	}
	public LocalDateTime getValidUntil() {
		return validUntil;
	}
	public void setValidUntil(LocalDateTime validUntil) {
		this.validUntil = validUntil;
	}
	public LocalDateTime getCalculatedAt() {
		return calculatedAt;
	}
	public void setCalculatedAt(LocalDateTime calculatedAt) {
		this.calculatedAt = calculatedAt;
	}
    

}
