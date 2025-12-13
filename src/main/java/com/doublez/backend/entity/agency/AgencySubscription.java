package com.doublez.backend.entity.agency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.doublez.backend.entity.credit.CreditPackage;
import com.doublez.backend.enums.subscription.SubscriptionPeriod;
import com.doublez.backend.enums.subscription.SubscriptionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "agency_subscriptions")
public class AgencySubscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_package_id", nullable = false)
    private CreditPackage creditPackage;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "period", nullable = false)
    private SubscriptionPeriod period = SubscriptionPeriod.MONTHLY;
    
    @Column(name = "monthly_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyPrice;
    
    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(name = "number_of_agents", nullable = false)
    private Integer numberOfAgents = 1;
    
    @Column(name = "number_of_super_agents", nullable = false)
    private Integer numberOfSuperAgents = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.PENDING;
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "next_billing_date")
    private LocalDateTime nextBillingDate;
    
    @Column(name = "auto_renew", nullable = false)
    private Boolean autoRenew = true;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "cancellation_reason")
    private String cancellationReason;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // ========================
    // CONSTRUCTORS
    // ========================
    
    public AgencySubscription() {}
    
    public AgencySubscription(Agency agency, CreditPackage creditPackage, 
                             SubscriptionPeriod period, Integer numberOfAgents, 
                             Integer numberOfSuperAgents) {
        this.agency = agency;
        this.creditPackage = creditPackage;
        this.period = period;
        this.numberOfAgents = numberOfAgents;
        this.numberOfSuperAgents = numberOfSuperAgents;
        this.monthlyPrice = calculateMonthlyPrice();
        this.totalPrice = period.calculateTotalPrice(monthlyPrice);
        this.status = SubscriptionStatus.PENDING;
        this.autoRenew = true;
    }
    
    // ========================
    // LIFECYCLE METHODS
    // ========================
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // ========================
    // BUSINESS METHODS
    // ========================
    
    private BigDecimal calculateMonthlyPrice() {
        // Base price from credit package
        BigDecimal basePrice = creditPackage.getPrice();
        
        // Add agent costs
        BigDecimal agentCost = BigDecimal.ZERO;
        if (numberOfAgents > 1) {
            // First agent is included, additional agents cost extra
            int additionalAgents = Math.max(0, numberOfAgents - 1);
            BigDecimal perAgentCost = creditPackage.getPerAgentMonthlyCost() != null ? 
                    creditPackage.getPerAgentMonthlyCost() : BigDecimal.valueOf(500);
            agentCost = perAgentCost.multiply(BigDecimal.valueOf(additionalAgents));
        }
        
        // Add super agent costs
        BigDecimal superAgentCost = BigDecimal.ZERO;
        if (numberOfSuperAgents > 0) {
            BigDecimal perSuperAgentCost = creditPackage.getPerSuperAgentMonthlyCost() != null ? 
                    creditPackage.getPerSuperAgentMonthlyCost() : BigDecimal.valueOf(1000);
            superAgentCost = perSuperAgentCost.multiply(BigDecimal.valueOf(numberOfSuperAgents));
        }
        
        return basePrice.add(agentCost).add(superAgentCost);
    }
    
    public void activate() {
        this.status = SubscriptionStatus.ACTIVE;
        this.startDate = LocalDateTime.now();
        this.endDate = startDate.plusMonths(period.getMonths());
        this.nextBillingDate = endDate;
    }
    
    public void cancel(String reason) {
        this.status = SubscriptionStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
        this.autoRenew = false;
    }
    
    public void pause() {
        this.status = SubscriptionStatus.PAUSED;
    }
    
    public void resume() {
        this.status = SubscriptionStatus.ACTIVE;
    }
    
    public void renew() {
        if (status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.EXPIRING) {
            this.startDate = LocalDateTime.now();
            this.endDate = startDate.plusMonths(period.getMonths());
            this.nextBillingDate = endDate;
            this.status = SubscriptionStatus.ACTIVE;
        }
    }
    
    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
    }
    
    public void markAsExpiring() {
        this.status = SubscriptionStatus.EXPIRING;
    }
    
    public boolean isActive() {
        return status.isActive();
    }
    
    public boolean isExpired() {
        return status == SubscriptionStatus.EXPIRED || 
               (endDate != null && LocalDateTime.now().isAfter(endDate));
    }
    
    public long getDaysUntilExpiry() {
        if (endDate == null) return 0;
        return java.time.Duration.between(LocalDateTime.now(), endDate).toDays();
    }
    
    public boolean shouldRenew() {
        return autoRenew && isActive() && getDaysUntilExpiry() <= 7;
    }
    
    public void updateAgentCount(Integer newAgentCount, Integer newSuperAgentCount) {
        this.numberOfAgents = newAgentCount;
        this.numberOfSuperAgents = newSuperAgentCount;
        this.monthlyPrice = calculateMonthlyPrice();
        this.totalPrice = period.calculateTotalPrice(monthlyPrice);
    }
    
    // ========================
    // GETTERS AND SETTERS
    // ========================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Agency getAgency() { return agency; }
    public void setAgency(Agency agency) { this.agency = agency; }
    
    public CreditPackage getCreditPackage() { return creditPackage; }
    public void setCreditPackage(CreditPackage creditPackage) { this.creditPackage = creditPackage; }
    
    public SubscriptionPeriod getPeriod() { return period; }
    public void setPeriod(SubscriptionPeriod period) { 
        this.period = period; 
        this.totalPrice = period.calculateTotalPrice(monthlyPrice);
    }
    
    public BigDecimal getMonthlyPrice() { return monthlyPrice; }
    public void setMonthlyPrice(BigDecimal monthlyPrice) { 
        this.monthlyPrice = monthlyPrice;
        this.totalPrice = period.calculateTotalPrice(monthlyPrice);
    }
    
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    
    public Integer getNumberOfAgents() { return numberOfAgents; }
    public void setNumberOfAgents(Integer numberOfAgents) { 
        this.numberOfAgents = numberOfAgents;
        this.monthlyPrice = calculateMonthlyPrice();
        this.totalPrice = period.calculateTotalPrice(monthlyPrice);
    }
    
    public Integer getNumberOfSuperAgents() { return numberOfSuperAgents; }
    public void setNumberOfSuperAgents(Integer numberOfSuperAgents) { 
        this.numberOfSuperAgents = numberOfSuperAgents;
        this.monthlyPrice = calculateMonthlyPrice();
        this.totalPrice = period.calculateTotalPrice(monthlyPrice);
    }
    
    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }
    
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public LocalDateTime getNextBillingDate() { return nextBillingDate; }
    public void setNextBillingDate(LocalDateTime nextBillingDate) { this.nextBillingDate = nextBillingDate; }
    
    public Boolean getAutoRenew() { return autoRenew; }
    public void setAutoRenew(Boolean autoRenew) { this.autoRenew = autoRenew; }
    
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
