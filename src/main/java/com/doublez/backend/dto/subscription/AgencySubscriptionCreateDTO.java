package com.doublez.backend.dto.subscription;

import com.doublez.backend.enums.subscription.SubscriptionPeriod;

public class AgencySubscriptionCreateDTO {
    private Long agencyId;
    private Long creditPackageId;
    private SubscriptionPeriod period = SubscriptionPeriod.MONTHLY;
    private Integer numberOfAgents;
    private Integer numberOfSuperAgents;
    private Boolean autoRenew = true;
    private String paymentMethod; // "BANK_TRANSFER", "CREDIT_CARD"
	
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
	public SubscriptionPeriod getPeriod() {
		return period;
	}
	public void setPeriod(SubscriptionPeriod period) {
		this.period = period;
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
	public Boolean getAutoRenew() {
		return autoRenew;
	}
	public void setAutoRenew(Boolean autoRenew) {
		this.autoRenew = autoRenew;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

    
}
