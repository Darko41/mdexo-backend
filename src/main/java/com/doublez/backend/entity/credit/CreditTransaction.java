package com.doublez.backend.entity.credit;

import java.time.LocalDateTime;

import com.doublez.backend.dto.credit.CreditTransactionResponseDTO;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.CreditTransactionType;
import com.doublez.backend.enums.PaymentStatus;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "credit_transactions")
public class CreditTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true) // Can be null for agency transactions
    private User user;

    @Column(name = "credit_change", nullable = false)
    private Integer creditChange; // Positive for additions, negative for deductions

    @Column(name = "balance_after", nullable = true)
    private Integer balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private CreditTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "reference_number")
    private String referenceNumber; // Bank reference or payment ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_package_id")
    private CreditPackage creditPackage;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON for additional data

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    // For agency-wide transactions
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id")
    private Agency agency;

    // ========================
    // CONSTRUCTORS (FIXED - No Duplicates)
    // ========================
    
    // Default constructor
    public CreditTransaction() {
        this.createdAt = LocalDateTime.now();
        this.paymentStatus = PaymentStatus.PENDING;
    }
    
    // Constructor 1: Basic transaction with user
    public CreditTransaction(User user, Integer creditChange, CreditTransactionType transactionType, String description) {
        this();
        this.user = user;
        this.creditChange = creditChange;
        this.transactionType = transactionType;
        this.description = description;
    }
    
    // Constructor 2: Transaction with agency
    public CreditTransaction(User user, Integer creditChange, CreditTransactionType transactionType, 
                           String description, Agency agency) {
        this(user, creditChange, transactionType, description);
        this.agency = agency;
    }
    
    // Constructor 3: Transaction with credit package
    public CreditTransaction(User user, CreditPackage creditPackage, Integer creditChange, 
                           CreditTransactionType transactionType, String description) {
        this(user, creditChange, transactionType, description);
        this.creditPackage = creditPackage;
    }
    
    // Constructor 4: Complete transaction
    public CreditTransaction(User user, Integer creditChange, Integer balanceAfter,
                           CreditTransactionType transactionType, PaymentStatus paymentStatus,
                           String description, String referenceNumber) {
        this();
        this.user = user;
        this.creditChange = creditChange;
        this.balanceAfter = balanceAfter;
        this.transactionType = transactionType;
        this.paymentStatus = paymentStatus;
        this.description = description;
        this.referenceNumber = referenceNumber;
    }

    // ========================
    // LIFECYCLE METHODS
    // ========================

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = PaymentStatus.PENDING;
        }
    }

    // ========================
    // BUSINESS METHODS
    // ========================

    public boolean isCreditAddition() {
        return creditChange != null && creditChange > 0;
    }

    public boolean isCreditDeduction() {
        return creditChange != null && creditChange < 0;
    }

    public void completeTransaction() {
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    public void completeTransaction(Integer balanceAfter) {
        this.balanceAfter = balanceAfter;
        completeTransaction();
    }

    public void failTransaction(String notes) {
        this.paymentStatus = PaymentStatus.FAILED;
        this.adminNotes = notes;
        this.processedAt = LocalDateTime.now();
    }

    public void refundTransaction(String notes) {
        this.paymentStatus = PaymentStatus.REFUNDED;
        this.adminNotes = notes;
        this.processedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return paymentStatus == PaymentStatus.PENDING;
    }

    public boolean isCompleted() {
        return paymentStatus == PaymentStatus.COMPLETED;
    }

    public boolean isFailed() {
        return paymentStatus == PaymentStatus.FAILED;
    }

    public CreditTransactionResponseDTO toDTO() {
        CreditTransactionResponseDTO dto = new CreditTransactionResponseDTO();
        dto.setId(this.id);
        dto.setCreditChange(this.creditChange);
        dto.setBalanceAfter(this.balanceAfter);
        dto.setTransactionType(this.transactionType);
        dto.setPaymentStatus(this.paymentStatus);
        dto.setDescription(this.description);
        dto.setReferenceNumber(this.referenceNumber);
        dto.setCreatedAt(this.createdAt);
        dto.setProcessedAt(this.processedAt);
        dto.setAdminNotes(this.adminNotes);
        dto.setMetadata(this.metadata);
        
        // Add user info
        if (this.user != null) {
            dto.setUserId(this.user.getId());
            dto.setUserEmail(this.user.getEmail());
        }
        
        // Add package info
        if (this.creditPackage != null) {
            dto.setCreditPackageId(this.creditPackage.getId());
            dto.setCreditPackageName(this.creditPackage.getName());
        }
        
        // Add agency info if present
        if (this.agency != null) {
            dto.setAgencyId(this.agency.getId());
            dto.setAgencyName(this.agency.getName());
        }
        
        return dto;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getCreditChange() { return creditChange; }
    public void setCreditChange(Integer creditChange) { this.creditChange = creditChange; }

    public Integer getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(Integer balanceAfter) { this.balanceAfter = balanceAfter; }

    public CreditTransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(CreditTransactionType transactionType) { this.transactionType = transactionType; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public CreditPackage getCreditPackage() { return creditPackage; }
    public void setCreditPackage(CreditPackage creditPackage) { this.creditPackage = creditPackage; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }

    public Agency getAgency() { return agency; }
    public void setAgency(Agency agency) { this.agency = agency; }
    
    // Helper method for notes (for backward compatibility)
    public void setNotes(String notes) {
        this.adminNotes = notes;
    }
    
    public String getNotes() {
        return this.adminNotes;
    }

    @Override
    public String toString() {
        return "CreditTransaction{" +
                "id=" + id +
                ", creditChange=" + creditChange +
                ", transactionType=" + transactionType +
                ", paymentStatus=" + paymentStatus +
                '}';
    }
}