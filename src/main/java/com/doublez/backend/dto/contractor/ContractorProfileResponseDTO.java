package com.doublez.backend.dto.contractor;

import java.math.BigDecimal;
import java.util.List;

import com.doublez.backend.dto.user.UserResponseDTO;

public class ContractorProfileResponseDTO {
    private Long id;
    private String companyName;
    private String pib;
    private String mb;
    private String website;
    private String contactPerson;
    private String phoneNumber;
    private List<String> serviceCategories;
    private List<String> serviceAreas;
    private Integer yearsExperience;
    private String portfolioDescription;
    private List<String> certifications;
    private String insuranceInfo;
    private BigDecimal hourlyRate;
    private BigDecimal minProjectSize;
    private Boolean isVisible;
    private Boolean isFeatured;
    private Double rating;
    private Integer reviewCount;
    private Integer completedProjects;
    private UserResponseDTO user;

    // Constructors
    public ContractorProfileResponseDTO() {}

    public ContractorProfileResponseDTO(Long id, String companyName, String pib, String mb, 
                                       String website, String contactPerson, String phoneNumber) {
        this.id = id;
        this.companyName = companyName;
        this.pib = pib;
        this.mb = mb;
        this.website = website;
        this.contactPerson = contactPerson;
        this.phoneNumber = phoneNumber;
    }

    // Helper methods
    public boolean hasCertifications() {
        return certifications != null && !certifications.isEmpty();
    }

    public boolean hasInsurance() {
        return insuranceInfo != null && !insuranceInfo.trim().isEmpty();
    }

    public boolean isProfileComplete() {
        return companyName != null && !companyName.trim().isEmpty() &&
               serviceCategories != null && !serviceCategories.isEmpty() &&
               serviceAreas != null && !serviceAreas.isEmpty() &&
               yearsExperience != null && yearsExperience > 0;
    }

    public boolean isEligibleForFeatured() {
        return isProfileComplete() && 
               hasCertifications() && 
               hasInsurance() && 
               rating != null && rating >= 4.0 &&
               reviewCount != null && reviewCount >= 5;
    }

    public String getRatingDisplay() {
        if (rating == null || reviewCount == null || reviewCount == 0) {
            return "No ratings yet";
        }
        return String.format("%.1f (%d reviews)", rating, reviewCount);
    }

    public boolean hasPricingInfo() {
        return hourlyRate != null || minProjectSize != null;
    }

    public String getExperienceDisplay() {
        if (yearsExperience == null) return "Experience not specified";
        if (yearsExperience == 1) return "1 year experience";
        return yearsExperience + " years experience";
    }

    public Integer getTotalServices() {
        return serviceCategories != null ? serviceCategories.size() : 0;
    }

    public Integer getTotalServiceAreas() {
        return serviceAreas != null ? serviceAreas.size() : 0;
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
    public List<String> getServiceCategories() { return serviceCategories; }
    public void setServiceCategories(List<String> serviceCategories) { this.serviceCategories = serviceCategories; }
    public List<String> getServiceAreas() { return serviceAreas; }
    public void setServiceAreas(List<String> serviceAreas) { this.serviceAreas = serviceAreas; }
    public Integer getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(Integer yearsExperience) { this.yearsExperience = yearsExperience; }
    public String getPortfolioDescription() { return portfolioDescription; }
    public void setPortfolioDescription(String portfolioDescription) { this.portfolioDescription = portfolioDescription; }
    public List<String> getCertifications() { return certifications; }
    public void setCertifications(List<String> certifications) { this.certifications = certifications; }
    public String getInsuranceInfo() { return insuranceInfo; }
    public void setInsuranceInfo(String insuranceInfo) { this.insuranceInfo = insuranceInfo; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
    public BigDecimal getMinProjectSize() { return minProjectSize; }
    public void setMinProjectSize(BigDecimal minProjectSize) { this.minProjectSize = minProjectSize; }
    public Boolean getIsVisible() { return isVisible; }
    public void setIsVisible(Boolean isVisible) { this.isVisible = isVisible; }
    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    public Integer getCompletedProjects() { return completedProjects; }
    public void setCompletedProjects(Integer completedProjects) { this.completedProjects = completedProjects; }
    public UserResponseDTO getUser() { return user; }
    public void setUser(UserResponseDTO user) { this.user = user; }
}
