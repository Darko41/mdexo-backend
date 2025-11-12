package com.doublez.backend.dto;

import java.math.BigDecimal;

import com.doublez.backend.enums.InvestorType;

public class InvestorProfileDTO {
	private String companyName;
	private String pib;
	private String mb;
	private String website;
	private String contactPerson;
	private String phoneNumber;
	private InvestorType investorType;
	private Integer yearsInBusiness;
	private Integer portfolioSize;
	private String investmentFocus;
	private String preferredLocations;
	private BigDecimal minInvestmentAmount;
	private BigDecimal maxInvestmentAmount;

	// Constructors
	public InvestorProfileDTO() {
	}

	public InvestorProfileDTO(String companyName, String pib, String mb, String website, String contactPerson,
			String phoneNumber, InvestorType investorType, Integer yearsInBusiness, Integer portfolioSize,
			String investmentFocus, String preferredLocations, BigDecimal minInvestmentAmount,
			BigDecimal maxInvestmentAmount) {
		this.companyName = companyName;
		this.pib = pib;
		this.mb = mb;
		this.website = website;
		this.contactPerson = contactPerson;
		this.phoneNumber = phoneNumber;
		this.investorType = investorType;
		this.yearsInBusiness = yearsInBusiness;
		this.portfolioSize = portfolioSize;
		this.investmentFocus = investmentFocus;
		this.preferredLocations = preferredLocations;
		this.minInvestmentAmount = minInvestmentAmount;
		this.maxInvestmentAmount = maxInvestmentAmount;
	}

	// Getters and Setters
	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getPib() {
		return pib;
	}

	public void setPib(String pib) {
		this.pib = pib;
	}

	public String getMb() {
		return mb;
	}

	public void setMb(String mb) {
		this.mb = mb;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getContactPerson() {
		return contactPerson;
	}

	public void setContactPerson(String contactPerson) {
		this.contactPerson = contactPerson;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public InvestorType getInvestorType() {
		return investorType;
	}

	public void setInvestorType(InvestorType investorType) {
		this.investorType = investorType;
	}

	public Integer getYearsInBusiness() {
		return yearsInBusiness;
	}

	public void setYearsInBusiness(Integer yearsInBusiness) {
		this.yearsInBusiness = yearsInBusiness;
	}

	public Integer getPortfolioSize() {
		return portfolioSize;
	}

	public void setPortfolioSize(Integer portfolioSize) {
		this.portfolioSize = portfolioSize;
	}

	public String getInvestmentFocus() {
		return investmentFocus;
	}

	public void setInvestmentFocus(String investmentFocus) {
		this.investmentFocus = investmentFocus;
	}

	public String getPreferredLocations() {
		return preferredLocations;
	}

	public void setPreferredLocations(String preferredLocations) {
		this.preferredLocations = preferredLocations;
	}

	public BigDecimal getMinInvestmentAmount() {
		return minInvestmentAmount;
	}

	public void setMinInvestmentAmount(BigDecimal minInvestmentAmount) {
		this.minInvestmentAmount = minInvestmentAmount;
	}

	public BigDecimal getMaxInvestmentAmount() {
		return maxInvestmentAmount;
	}

	public void setMaxInvestmentAmount(BigDecimal maxInvestmentAmount) {
		this.maxInvestmentAmount = maxInvestmentAmount;
	}
}
