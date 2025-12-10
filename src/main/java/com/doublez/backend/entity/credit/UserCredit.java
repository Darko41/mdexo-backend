package com.doublez.backend.entity.credit;

import java.time.LocalDateTime;

import com.doublez.backend.dto.credit.CreditBalanceDTO;
import com.doublez.backend.entity.user.User;

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
@Table(name = "user_credits")
public class UserCredit {

    @Id
    private Long id;

    @Column(name = "current_balance", nullable = false)
    private Integer currentBalance = 0;

    @Column(name = "lifetime_credits_earned", nullable = false)
    private Integer lifetimeCreditsEarned = 0;

    @Column(name = "lifetime_credits_spent", nullable = false)
    private Integer lifetimeCreditsSpent = 0;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    // ========================
    // CONSTRUCTORS
    // ========================

    public UserCredit() {
    }

    public UserCredit(User user) {
        this.user = user;
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

    public void addCredits(Integer credits, String reason) {
        if (credits > 0) {
            this.currentBalance += credits;
            this.lifetimeCreditsEarned += credits;
            updateTimestamp();
        }
    }

    public boolean deductCredits(Integer credits, String reason) {
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

    public void resetBalance() {
        this.currentBalance = 0;
        updateTimestamp();
    }

    public CreditBalanceDTO toBalanceDTO() {
        return new CreditBalanceDTO(
            this.currentBalance,
            this.lifetimeCreditsEarned,
            this.lifetimeCreditsSpent,
            this.lastUpdated
        );
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

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    @Override
    public String toString() {
        return "UserCredit{" +
                "id=" + id +
                ", currentBalance=" + currentBalance +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
