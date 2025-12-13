package com.doublez.backend.dto.credit;

import java.math.BigDecimal;

import com.doublez.backend.enums.subscription.SubscriptionPeriod;

public class AgencyCreditPurchaseDTO {
    private Long agencyId;
    private Long creditPackageId;
    private Integer numberOfAgents;
    private Integer numberOfSuperAgents;
    private SubscriptionPeriod period = SubscriptionPeriod.MONTHLY;
    private String paymentMethod = "BANK_TRANSFER"; // or "CREDIT_CARD"
    private Boolean autoRenew = false;
    private Boolean distributeToTeam = true; // Distribute credits to all agents
    private String notes;
    
    // CALCULATED (client can send for verification)
    private BigDecimal expectedTotalPrice;
    private Integer expectedTotalCredits;
    
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
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public Boolean getAutoRenew() {
		return autoRenew;
	}
	public void setAutoRenew(Boolean autoRenew) {
		this.autoRenew = autoRenew;
	}
	public Boolean getDistributeToTeam() {
		return distributeToTeam;
	}
	public void setDistributeToTeam(Boolean distributeToTeam) {
		this.distributeToTeam = distributeToTeam;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public BigDecimal getExpectedTotalPrice() {
		return expectedTotalPrice;
	}
	public void setExpectedTotalPrice(BigDecimal expectedTotalPrice) {
		this.expectedTotalPrice = expectedTotalPrice;
	}
	public Integer getExpectedTotalCredits() {
		return expectedTotalCredits;
	}
	public void setExpectedTotalCredits(Integer expectedTotalCredits) {
		this.expectedTotalCredits = expectedTotalCredits;
	}
	
    

}
