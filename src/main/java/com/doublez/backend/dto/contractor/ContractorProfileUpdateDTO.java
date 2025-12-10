package com.doublez.backend.dto.contractor;

import java.math.BigDecimal;

public class ContractorProfileUpdateDTO {
    private String companyName;
    private String website;
    private String contactPerson;
    private String phoneNumber;
    private String serviceCategories;
    private String serviceAreas;
    private Integer yearsExperience;
    private String portfolioDescription;
    private String certifications;
    private String insuranceInfo;
    private BigDecimal hourlyRate;
    private BigDecimal minProjectSize;
    private Boolean isVisible;

    // Constructors
    public ContractorProfileUpdateDTO() {}

    // Helper methods
    public boolean hasUpdates() {
        return companyName != null || website != null || contactPerson != null || 
               phoneNumber != null || serviceCategories != null || serviceAreas != null ||
               yearsExperience != null || portfolioDescription != null || certifications != null ||
               insuranceInfo != null || hourlyRate != null || minProjectSize != null || 
               isVisible != null;
    }

    public boolean hasServiceUpdates() {
        return serviceCategories != null || serviceAreas != null || yearsExperience != null;
    }

    public boolean hasBusinessUpdates() {
        return companyName != null || website != null || contactPerson != null || phoneNumber != null;
    }

    public boolean hasPricingUpdates() {
        return hourlyRate != null || minProjectSize != null;
    }

    // Getters and Setters
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getServiceCategories() { return serviceCategories; }
    public void setServiceCategories(String serviceCategories) { this.serviceCategories = serviceCategories; }
    public String getServiceAreas() { return serviceAreas; }
    public void setServiceAreas(String serviceAreas) { this.serviceAreas = serviceAreas; }
    public Integer getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(Integer yearsExperience) { this.yearsExperience = yearsExperience; }
    public String getPortfolioDescription() { return portfolioDescription; }
    public void setPortfolioDescription(String portfolioDescription) { this.portfolioDescription = portfolioDescription; }
    public String getCertifications() { return certifications; }
    public void setCertifications(String certifications) { this.certifications = certifications; }
    public String getInsuranceInfo() { return insuranceInfo; }
    public void setInsuranceInfo(String insuranceInfo) { this.insuranceInfo = insuranceInfo; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
    public BigDecimal getMinProjectSize() { return minProjectSize; }
    public void setMinProjectSize(BigDecimal minProjectSize) { this.minProjectSize = minProjectSize; }
    public Boolean getIsVisible() { return isVisible; }
    public void setIsVisible(Boolean isVisible) { this.isVisible = isVisible; }
}
