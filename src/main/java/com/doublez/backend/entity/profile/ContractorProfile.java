package com.doublez.backend.entity.profile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.doublez.backend.entity.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "contractor_profiles")
public class ContractorProfile {

    @Id
    private Long id;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "pib", length = 9) // Serbian Tax ID
    private String pib;

    @Column(name = "mb", length = 8) // Serbian Business ID
    private String mb;

    @Column(name = "website")
    private String website;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "service_categories")
    private String serviceCategories; // JSON or comma-separated: "renovation,plumbing,electrical"

    @Column(name = "service_areas", length = 1000)
    private String serviceAreas; // JSON or comma-separated locations

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Column(name = "portfolio_description", columnDefinition = "TEXT")
    private String portfolioDescription;

    @Column(name = "certifications", columnDefinition = "TEXT")
    private String certifications; // JSON or comma-separated certifications

    @Column(name = "insurance_info")
    private String insuranceInfo;

    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;

    @Column(name = "min_project_size")
    private BigDecimal minProjectSize;

    @Column(name = "is_visible")
    private Boolean isVisible = false; // Profile visibility (€9.90/month)

    @Column(name = "is_featured")
    private Boolean isFeatured = false; // Featured profile status

    @Column(name = "rating")
    private Double rating = 0.0;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "completed_projects")
    private Integer completedProjects = 0;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    // ========================
    // CONSTRUCTORS
    // ========================

    public ContractorProfile() {
    }

    public ContractorProfile(User user) {
        this.user = user;
    }

    // ========================
    // BUSINESS METHODS
    // ========================

    public void addReview(Double newRating) {
        if (this.rating == null) {
            this.rating = newRating;
            this.reviewCount = 1;
        } else {
            double totalRating = this.rating * this.reviewCount;
            totalRating += newRating;
            this.reviewCount++;
            this.rating = totalRating / this.reviewCount;
        }
    }

    public void incrementCompletedProjects() {
        this.completedProjects = (this.completedProjects == null) ? 1 : this.completedProjects + 1;
    }

    public boolean hasCertifications() {
        return certifications != null && !certifications.trim().isEmpty();
    }

    public boolean hasInsurance() {
        return insuranceInfo != null && !insuranceInfo.trim().isEmpty();
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

    // ========================
    // GETTERS AND SETTERS
    // ========================

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

    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }

    public Integer getCompletedProjects() { return completedProjects; }
    public void setCompletedProjects(Integer completedProjects) { this.completedProjects = completedProjects; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    @Override
    public String toString() {
        return "ContractorProfile{" +
                "id=" + id +
                ", companyName='" + companyName + '\'' +
                ", isVisible=" + isVisible +
                ", rating=" + rating +
                '}';
    }
}
