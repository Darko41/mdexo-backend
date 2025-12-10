package com.doublez.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.InvestorType;
import com.doublez.backend.enums.VerificationStatus;

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

    @Column(name = "pib", length = 9)
    private String pib;

    @Column(name = "mb", length = 8)
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
    private Integer portfolioSize = 0;

    @Column(name = "investment_focus", length = 500)
    private String investmentFocus;

    @Column(name = "preferred_locations", length = 1000)
    private String preferredLocations;

    @Column(name = "min_investment_amount")
    private BigDecimal minInvestmentAmount;

    @Column(name = "max_investment_amount")
    private BigDecimal maxInvestmentAmount;

    @Column(name = "completed_investments")
    private Integer completedInvestments = 0;

    @Column(name = "active_investments")
    private Integer activeInvestments = 0;

    @Column(name = "verification_status")
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    // ========================
    // CONSTRUCTORS
    // ========================

    public InvestorProfile() {
    }

    public InvestorProfile(User user) {
        this.user = user;
    }

    // ========================
    // BUSINESS METHODS
    // ========================

    public void incrementPortfolioSize() {
        this.portfolioSize = (this.portfolioSize == null) ? 1 : this.portfolioSize + 1;
    }

    public void incrementCompletedInvestments() {
        this.completedInvestments = (this.completedInvestments == null) ? 1 : this.completedInvestments + 1;
    }

    public void incrementActiveInvestments() {
        this.activeInvestments = (this.activeInvestments == null) ? 1 : this.activeInvestments + 1;
    }

    public void verify() {
        this.verificationStatus = VerificationStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }

    public boolean isVerified() {
        return verificationStatus == VerificationStatus.VERIFIED;
    }

    public List<String> getPreferredLocationsList() {
        if (preferredLocations == null || preferredLocations.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(preferredLocations.split(","));
    }

    public boolean hasCompanyInfo() {
        return companyName != null && !companyName.trim().isEmpty() &&
               pib != null && !pib.trim().isEmpty();
    }

    

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

	public Integer getCompletedInvestments() {
		return completedInvestments;
	}

	public void setCompletedInvestments(Integer completedInvestments) {
		this.completedInvestments = completedInvestments;
	}

	public Integer getActiveInvestments() {
		return activeInvestments;
	}

	public void setActiveInvestments(Integer activeInvestments) {
		this.activeInvestments = activeInvestments;
	}

	public VerificationStatus getVerificationStatus() {
		return verificationStatus;
	}

	public void setVerificationStatus(VerificationStatus verificationStatus) {
		this.verificationStatus = verificationStatus;
	}

	public LocalDateTime getVerifiedAt() {
		return verifiedAt;
	}

	public void setVerifiedAt(LocalDateTime verifiedAt) {
		this.verifiedAt = verifiedAt;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
    public String toString() {
        return "InvestorProfile{" +
                "id=" + id +
                ", companyName='" + companyName + '\'' +
                ", investorType=" + investorType +
                ", portfolioSize=" + portfolioSize +
                ", verificationStatus=" + verificationStatus +
                '}';
    }
}