package com.doublez.backend.dto.subscription;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.doublez.backend.enums.subscription.SubscriptionPeriod;
import com.doublez.backend.enums.subscription.SubscriptionStatus;

public class AgencySubscriptionResponseDTO {
    private Long id;
    private Long agencyId;
    private String agencyName;
    private Long creditPackageId;
    private String packageName;
    private SubscriptionPeriod period;
    private BigDecimal monthlyPrice;
    private BigDecimal totalPrice;
    private Integer numberOfAgents;
    private Integer numberOfSuperAgents;
    private Integer maxAgentsAllowed;
    private Integer maxSuperAgentsAllowed;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime nextBillingDate;
    private Boolean autoRenew;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
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
	public SubscriptionPeriod getPeriod() {
		return period;
	}
	public void setPeriod(SubscriptionPeriod period) {
		this.period = period;
	}
	public BigDecimal getMonthlyPrice() {
		return monthlyPrice;
	}
	public void setMonthlyPrice(BigDecimal monthlyPrice) {
		this.monthlyPrice = monthlyPrice;
	}
	public BigDecimal getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
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
	public Integer getMaxAgentsAllowed() {
		return maxAgentsAllowed;
	}
	public void setMaxAgentsAllowed(Integer maxAgentsAllowed) {
		this.maxAgentsAllowed = maxAgentsAllowed;
	}
	public Integer getMaxSuperAgentsAllowed() {
		return maxSuperAgentsAllowed;
	}
	public void setMaxSuperAgentsAllowed(Integer maxSuperAgentsAllowed) {
		this.maxSuperAgentsAllowed = maxSuperAgentsAllowed;
	}
	public SubscriptionStatus getStatus() {
		return status;
	}
	public void setStatus(SubscriptionStatus status) {
		this.status = status;
	}
	public LocalDateTime getStartDate() {
		return startDate;
	}
	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}
	public LocalDateTime getEndDate() {
		return endDate;
	}
	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}
	public LocalDateTime getNextBillingDate() {
		return nextBillingDate;
	}
	public void setNextBillingDate(LocalDateTime nextBillingDate) {
		this.nextBillingDate = nextBillingDate;
	}
	public Boolean getAutoRenew() {
		return autoRenew;
	}
	public void setAutoRenew(Boolean autoRenew) {
		this.autoRenew = autoRenew;
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
    
    // getters and setters
    
}
