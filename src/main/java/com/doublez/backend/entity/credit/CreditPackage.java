package com.doublez.backend.entity.credit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import com.doublez.backend.enums.CreditPackageType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "credit_packages")
public class CreditPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditPackageType type;

    @Column(name = "credit_amount", nullable = false)
    private Integer creditAmount;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price; // null for free packages

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "duration_days")
    private Integer durationDays; // For subscriptions/features

    @Column(name = "recurring")
    private Boolean recurring = false; // For monthly subscriptions

    @Column(name = "features_included")
    private String featuresIncluded; // JSON description of features

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========================
    // CONSTRUCTORS
    // ========================

    public CreditPackage() {
    }

    public CreditPackage(String name, CreditPackageType type, Integer creditAmount, BigDecimal price) {
        this.name = name;
        this.type = type;
        this.creditAmount = creditAmount;
        this.price = price;
        this.isActive = true;
    }

    // ========================
    // LIFECYCLE METHODS
    // ========================

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ========================
    // BUSINESS METHODS
    // ========================

    public boolean isFree() {
        return price == null || price.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isSubscription() {
        return type == CreditPackageType.TIER_SUBSCRIPTION && Boolean.TRUE.equals(recurring);
    }

    public boolean isOneTimePurchase() {
        return type == CreditPackageType.ONE_TIME || type == CreditPackageType.FEATURE;
    }

    public String getDisplayPrice() {
        if (isFree()) {
            return "FREE";
        }
        return "€" + price.toString();
    }

    public BigDecimal getPricePerCredit() {
        if (isFree() || creditAmount == 0) {
            return BigDecimal.ZERO;
        }
        return price.divide(BigDecimal.valueOf(creditAmount), 2, RoundingMode.HALF_UP);
    }

    // ========================
    // GETTERS AND SETTERS
    // ========================
@Override
    public String toString() {
        return "CreditPackage{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", creditAmount=" + creditAmount +
                ", price=" + price +
                ", type=" + type +
                '}';
    }
}

