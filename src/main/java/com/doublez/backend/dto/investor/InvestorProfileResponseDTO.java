package com.doublez.backend.dto.investor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.doublez.backend.enums.InvestorType;
import com.doublez.backend.enums.VerificationStatus;

public class InvestorProfileResponseDTO {
    private Long id;
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
    private List<String> preferredLocations;
    private BigDecimal minInvestmentAmount;
    private BigDecimal maxInvestmentAmount;
    private Integer completedInvestments;
    private Integer activeInvestments;
    private VerificationStatus verificationStatus;
    private LocalDateTime verifiedAt;

    // Constructors
    public InvestorProfileResponseDTO() {}

    // Helper methods
    public boolean isVerified() {
        return verificationStatus == VerificationStatus.VERIFIED;
    }

    public boolean hasCompanyInfo() {
        return companyName != null && !companyName.trim().isEmpty() &&
               pib != null && !pib.trim().isEmpty();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getPib() { return pib; }
    public void setPib(String pib) { this.pib = pib; }
    public String getMb() { return mb; }
    public void setMb(String mb) { this.mb = mb; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public InvestorType getInvestorType() { return investorType; }
    public void setInvestorType(InvestorType investorType) { this.investorType = investorType; }
    public Integer getYearsInBusiness() { return yearsInBusiness; }
    public void setYearsInBusiness(Integer yearsInBusiness) { this.yearsInBusiness = yearsInBusiness; }
    public Integer getPortfolioSize() { return portfolioSize; }
    public void setPortfolioSize(Integer portfolioSize) { this.portfolioSize = portfolioSize; }
    public String getInvestmentFocus() { return investmentFocus; }
    public void setInvestmentFocus(String investmentFocus) { this.investmentFocus = investmentFocus; }
    public List<String> getPreferredLocations() { return preferredLocations; }
    public void setPreferredLocations(List<String> preferredLocations) { this.preferredLocations = preferredLocations; }
    public BigDecimal getMinInvestmentAmount() { return minInvestmentAmount; }
    public void setMinInvestmentAmount(BigDecimal minInvestmentAmount) { this.minInvestmentAmount = minInvestmentAmount; }
    public BigDecimal getMaxInvestmentAmount() { return maxInvestmentAmount; }
    public void setMaxInvestmentAmount(BigDecimal maxInvestmentAmount) { this.maxInvestmentAmount = maxInvestmentAmount; }
    public Integer getCompletedInvestments() { return completedInvestments; }
    public void setCompletedInvestments(Integer completedInvestments) { this.completedInvestments = completedInvestments; }
    public Integer getActiveInvestments() { return activeInvestments; }
    public void setActiveInvestments(Integer activeInvestments) { this.activeInvestments = activeInvestments; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
}