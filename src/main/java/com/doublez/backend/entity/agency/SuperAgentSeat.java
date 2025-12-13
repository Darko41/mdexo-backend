package com.doublez.backend.entity.agency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "super_agent_seats")
public class SuperAgentSeat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private AgencySubscription subscription;
    
    @Column(name = "purchased_at", nullable = false)
    private LocalDateTime purchasedAt;
    
    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "price_paid", precision = 15, scale = 2)
    private BigDecimal pricePaid;
    
    // ========================
    // CONSTRUCTORS
    // ========================
    
    public SuperAgentSeat() {}
    
    public SuperAgentSeat(Agency agency, Agent agent, AgencySubscription subscription, 
                         LocalDateTime validUntil, BigDecimal pricePaid) {
        this.agency = agency;
        this.agent = agent;
        this.subscription = subscription;
        this.purchasedAt = LocalDateTime.now();
        this.validUntil = validUntil;
        this.pricePaid = pricePaid;
        this.isActive = true;
    }
    
    // ========================
    // BUSINESS METHODS
    // ========================
    
    public boolean isValid() {
        return isActive && LocalDateTime.now().isBefore(validUntil);
    }
    
    public long getDaysRemaining() {
        if (!isValid()) return 0;
        return java.time.Duration.between(LocalDateTime.now(), validUntil).toDays();
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public void extend(LocalDateTime newValidUntil) {
        this.validUntil = newValidUntil;
        this.isActive = true;
    }
    
    // ========================
    // GETTERS AND SETTERS
    // ========================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Agency getAgency() { return agency; }
    public void setAgency(Agency agency) { this.agency = agency; }
    
    public Agent getAgent() { return agent; }
    public void setAgent(Agent agent) { this.agent = agent; }
    
    public AgencySubscription getSubscription() { return subscription; }
    public void setSubscription(AgencySubscription subscription) { this.subscription = subscription; }
    
    public LocalDateTime getPurchasedAt() { return purchasedAt; }
    public void setPurchasedAt(LocalDateTime purchasedAt) { this.purchasedAt = purchasedAt; }
    
    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public BigDecimal getPricePaid() { return pricePaid; }
    public void setPricePaid(BigDecimal pricePaid) { this.pricePaid = pricePaid; }
}
