package com.doublez.backend.entity.credit;

import java.time.LocalDateTime;
import java.util.List;

import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.Agent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "agency_credits")
public class AgencyCredit {
    
    @Id
    private Long id;
    
    @Column(name = "current_balance", nullable = false)
    private Integer currentBalance = 0;
    
    @Column(name = "lifetime_credits_earned", nullable = false)
    private Integer lifetimeCreditsEarned = 0;
    
    @Column(name = "lifetime_credits_spent", nullable = false)
    private Integer lifetimeCreditsSpent = 0;
    
    @Column(name = "team_distribution_enabled", nullable = false)
    private Boolean teamDistributionEnabled = true;
    
    @Column(name = "distribution_percentage", nullable = false)
    private Integer distributionPercentage = 100; // % of credits to distribute to agents
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Agency agency;
    
    // ========================
    // CONSTRUCTORS
    // ========================
    
    public AgencyCredit() {}
    
    public AgencyCredit(Agency agency) {
        this.agency = agency;
        this.currentBalance = 0;
        this.lastUpdated = LocalDateTime.now();
    }
    
    // ========================
    // LIFECYCLE METHODS
    // ========================
    
    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.lastUpdated = LocalDateTime.now();
    }
    
    // ========================
    // BUSINESS METHODS
    // ========================
    
    public void addCredits(Integer credits) {
        if (credits > 0) {
            this.currentBalance += credits;
            this.lifetimeCreditsEarned += credits;
            updateTimestamp();
        }
    }
    
    public boolean deductCredits(Integer credits) {
        if (credits > 0 && hasSufficientCredits(credits)) {
            this.currentBalance -= credits;
            this.lifetimeCreditsSpent += credits;
            updateTimestamp();
            return true;
        }
        return false;
    }
    
    public boolean hasSufficientCredits(Integer requiredCredits) {
        return this.currentBalance >= requiredCredits;
    }
    
    public Integer getAvailableCredits() {
        return this.currentBalance;
    }
    
    /**
     * Distribute credits to team agents based on distribution settings
     */
    public Integer distributeToTeam(List<Agent> agents) {
        if (!teamDistributionEnabled || distributionPercentage <= 0 || agents.isEmpty()) {
            return 0;
        }
        
        int creditsToDistribute = (currentBalance * distributionPercentage) / 100;
        if (creditsToDistribute <= 0) {
            return 0;
        }
        
        int creditsPerAgent = creditsToDistribute / agents.size();
        if (creditsPerAgent <= 0) {
            return 0;
        }
        
        // We'll implement the actual distribution in service layer
        // This entity just tracks the agency pool
        return creditsToDistribute;
    }
    
    // ========================
    // GETTERS AND SETTERS
    // ========================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(Integer currentBalance) { this.currentBalance = currentBalance; }
    
    public Integer getLifetimeCreditsEarned() { return lifetimeCreditsEarned; }
    public void setLifetimeCreditsEarned(Integer lifetimeCreditsEarned) { this.lifetimeCreditsEarned = lifetimeCreditsEarned; }
    
    public Integer getLifetimeCreditsSpent() { return lifetimeCreditsSpent; }
    public void setLifetimeCreditsSpent(Integer lifetimeCreditsSpent) { this.lifetimeCreditsSpent = lifetimeCreditsSpent; }
    
    public Boolean getTeamDistributionEnabled() { return teamDistributionEnabled; }
    public void setTeamDistributionEnabled(Boolean teamDistributionEnabled) { this.teamDistributionEnabled = teamDistributionEnabled; }
    
    public Integer getDistributionPercentage() { return distributionPercentage; }
    public void setDistributionPercentage(Integer distributionPercentage) { 
        this.distributionPercentage = Math.min(100, Math.max(0, distributionPercentage));
    }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public Agency getAgency() { return agency; }
    public void setAgency(Agency agency) { this.agency = agency; }
}
