package com.doublez.backend.entity;

import java.math.BigDecimal;

import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.InvestorType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "investor_profiles")
public class InvestorProfile {

	@Id
	private Long id;

	@Column(name = "company_name")
	private String companyName;

	@Column(name = "pib", length = 9) // Serbian Tax ID (9 digits)
	private String pib;

	@Column(name = "mb", length = 8) // Serbian Registration Number (8 digits)
	private String mb;

	@Column(name = "website")
	private String website;

	@Column(name = "contact_person")
	private String contactPerson;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "investor_type")
	private InvestorType investorType;

	@Column(name = "years_in_business")
	private Integer yearsInBusiness;

	@Column(name = "portfolio_size")
	private Integer portfolioSize; // Number of properties in portfolio

	@Column(name = "investment_focus", length = 500)
	private String investmentFocus; // Description of investment strategy

	@Column(name = "preferred_locations", length = 1000)
	private String preferredLocations; // JSON or comma-separated locations

	@Column(name = "min_investment_amount")
	private BigDecimal minInvestmentAmount;

	@Column(name = "max_investment_amount")
	private BigDecimal maxInvestmentAmount;

	@OneToOne
	@MapsId
	@JoinColumn(name = "id")
	private User user;

	// Constructors
	public InvestorProfile() {
	}

	public InvestorProfile(User user) {
		this.user = user;
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
