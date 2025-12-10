package com.doublez.backend.dto.agent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.doublez.backend.enums.agency.AgentRole;

public class AgentResponseDTO {
    
    private Long id;
    private Long userId;
    private String userEmail;
    private String userFirstName;
    private String userLastName;
    private String userPhone;
    private String userProfileImage;
    private Long agencyId;
    private String agencyName;
    private String agencyLogo;
    private AgentRole role;
    private String roleDisplayName;
    private Boolean isActive;
    private LocalDateTime joinDate;
    private LocalDateTime lastActiveDate;
    private Integer maxListings;
    private Boolean canManageListings;
    private Boolean canViewAnalytics;
    private Boolean canManageBilling;
    private Boolean canInviteAgents;
    private BigDecimal commissionRate;
    private Integer totalListingsCreated;
    private Integer activeListingsCount;
    private Integer leadsGenerated;
    private Integer dealsClosed;
    private Double averageResponseTimeMinutes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Performance metrics
    private Double conversionRate; // deals/leads
    private String performanceRating; // Excellent, Good, Average, Poor
    private Integer remainingListingSlots;
    private Boolean hasReachedListingLimit;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getUserFirstName() { return userFirstName; }
    public void setUserFirstName(String userFirstName) { this.userFirstName = userFirstName; }
    
    public String getUserLastName() { return userLastName; }
    public void setUserLastName(String userLastName) { this.userLastName = userLastName; }
    
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    
    public String getUserProfileImage() { return userProfileImage; }
    public void setUserProfileImage(String userProfileImage) { this.userProfileImage = userProfileImage; }
    
    public Long getAgencyId() { return agencyId; }
    public void setAgencyId(Long agencyId) { this.agencyId = agencyId; }
    
    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }
    
    public String getAgencyLogo() { return agencyLogo; }
    public void setAgencyLogo(String agencyLogo) { this.agencyLogo = agencyLogo; }
    
    public AgentRole getRole() { return role; }
    public void setRole(AgentRole role) { 
        this.role = role;
        if (role != null) {
            this.roleDisplayName = role.getDisplayName();
        }
    }
    
    public String getRoleDisplayName() { return roleDisplayName; }
    public void setRoleDisplayName(String roleDisplayName) { this.roleDisplayName = roleDisplayName; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDateTime joinDate) { this.joinDate = joinDate; }
    
    public LocalDateTime getLastActiveDate() { return lastActiveDate; }
    public void setLastActiveDate(LocalDateTime lastActiveDate) { this.lastActiveDate = lastActiveDate; }
    
    public Integer getMaxListings() { return maxListings; }
    public void setMaxListings(Integer maxListings) { this.maxListings = maxListings; }
    
    public Boolean getCanManageListings() { return canManageListings; }
    public void setCanManageListings(Boolean canManageListings) { this.canManageListings = canManageListings; }
    
    public Boolean getCanViewAnalytics() { return canViewAnalytics; }
    public void setCanViewAnalytics(Boolean canViewAnalytics) { this.canViewAnalytics = canViewAnalytics; }
    
    public Boolean getCanManageBilling() { return canManageBilling; }
    public void setCanManageBilling(Boolean canManageBilling) { this.canManageBilling = canManageBilling; }
    
    public Boolean getCanInviteAgents() { return canInviteAgents; }
    public void setCanInviteAgents(Boolean canInviteAgents) { this.canInviteAgents = canInviteAgents; }
    
    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
    
    public Integer getTotalListingsCreated() { return totalListingsCreated; }
    public void setTotalListingsCreated(Integer totalListingsCreated) { this.totalListingsCreated = totalListingsCreated; }
    
    public Integer getActiveListingsCount() { return activeListingsCount; }
    public void setActiveListingsCount(Integer activeListingsCount) { this.activeListingsCount = activeListingsCount; }
    
    public Integer getLeadsGenerated() { return leadsGenerated; }
    public void setLeadsGenerated(Integer leadsGenerated) { this.leadsGenerated = leadsGenerated; }
    
    public Integer getDealsClosed() { return dealsClosed; }
    public void setDealsClosed(Integer dealsClosed) { this.dealsClosed = dealsClosed; }
    
    public Double getAverageResponseTimeMinutes() { return averageResponseTimeMinutes; }
    public void setAverageResponseTimeMinutes(Double averageResponseTimeMinutes) { this.averageResponseTimeMinutes = averageResponseTimeMinutes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Double getConversionRate() {
        if (leadsGenerated != null && leadsGenerated > 0 && dealsClosed != null) {
            return (double) dealsClosed / leadsGenerated * 100;
        }
        return 0.0;
    }
    
    public void setConversionRate(Double conversionRate) {
        // Read-only property, do nothing
    }
    
    public String getPerformanceRating() {
        if (dealsClosed == null || totalListingsCreated == null || totalListingsCreated == 0) {
            return "N/A";
        }
        double successRate = (double) dealsClosed / totalListingsCreated;
        if (successRate > 0.3) return "Excellent";
        if (successRate > 0.2) return "Good";
        if (successRate > 0.1) return "Average";
        return "Poor";
    }
    
    public void setPerformanceRating(String performanceRating) {
        // Read-only property, do nothing
    }
    
    public Integer getRemainingListingSlots() {
        if (maxListings == null) return null;
        return Math.max(0, maxListings - (activeListingsCount != null ? activeListingsCount : 0));
    }
    
    public void setRemainingListingSlots(Integer remainingListingSlots) {
        // Read-only property, do nothing
    }
    
    public Boolean getHasReachedListingLimit() {
        if (maxListings == null) return false;
        return activeListingsCount != null && activeListingsCount >= maxListings;
    }
    
    public void setHasReachedListingLimit(Boolean hasReachedListingLimit) {
        // Read-only property, do nothing
    }
}
