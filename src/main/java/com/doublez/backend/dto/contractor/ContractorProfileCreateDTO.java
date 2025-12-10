package com.doublez.backend.dto.contractor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.validation.constraints.Pattern;

public class ContractorProfileCreateDTO {
    private String companyName;
    private String pib;
    private String mb;
    private String website;
    private String contactPerson;
    
    @Pattern(regexp = "^[0-9\\s\\-\\/\\(\\)]{5,20}$")
    private String phoneNumber;
    
    private String serviceCategories; // JSON or comma-separated: "renovation,plumbing,electrical"
    private String serviceAreas; // JSON or comma-separated locations
    private Integer yearsExperience;
    private String portfolioDescription;
    private String certifications; // JSON or comma-separated certifications
    private String insuranceInfo;
    private BigDecimal hourlyRate;
    private BigDecimal minProjectSize;
    private Boolean isVisible = false; // Profile visibility (€9.90/month)

    // Constructors
    public ContractorProfileCreateDTO() {}

    public ContractorProfileCreateDTO(String companyName, String pib, String mb, String website, 
                                     String contactPerson, String phoneNumber, String serviceCategories, 
                                     String serviceAreas, Integer yearsExperience) {
        this.companyName = companyName;
        this.pib = pib;
        this.mb = mb;
        this.website = website;
        this.contactPerson = contactPerson;
        this.phoneNumber = phoneNumber;
        this.serviceCategories = serviceCategories;
        this.serviceAreas = serviceAreas;
        this.yearsExperience = yearsExperience;
    }

    // Helper methods
    public boolean hasCompanyInfo() {
        return companyName != null && !companyName.trim().isEmpty() &&
               pib != null && !pib.trim().isEmpty() &&
               mb != null && !mb.trim().isEmpty();
    }

    public boolean hasServicesInfo() {
        return serviceCategories != null && !serviceCategories.trim().isEmpty() &&
               serviceAreas != null && !serviceAreas.trim().isEmpty();
    }

    public boolean hasExperienceInfo() {
        return yearsExperience != null && yearsExperience > 0;
    }

    public List<String> getServiceCategoriesList() {
        if (serviceCategories == null || serviceCategories.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(serviceCategories.split(","));
    }

    public List<String> getServiceAreasList() {
        if (serviceAreas == null || serviceAreas.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(serviceAreas.split(","));
    }

    // Getters and Setters
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
