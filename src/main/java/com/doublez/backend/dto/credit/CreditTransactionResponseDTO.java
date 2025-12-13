package com.doublez.backend.dto.credit;

import java.time.LocalDateTime;

import com.doublez.backend.enums.CreditTransactionType;
import com.doublez.backend.enums.PaymentStatus;

public class CreditTransactionResponseDTO {
    private Long id;
    private Integer creditChange;
    private Integer balanceAfter;
    private CreditTransactionType transactionType;
    private PaymentStatus paymentStatus;
    private String description;
    private String referenceNumber;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String adminNotes;
    private String metadata;
    
    // User and package info
    private Long userId;
    private String userEmail;
    private Long creditPackageId;
    private String creditPackageName;
    
    // Agency info
    private Long agencyId;
    private String agencyName;

    // Computed fields
    private Boolean isCreditAddition;
    private Boolean isCreditDeduction;
    private Boolean isPending;
    private Boolean isCompleted;

    // Constructors
    public CreditTransactionResponseDTO() {}

    public CreditTransactionResponseDTO(Long id, Integer creditChange, Integer balanceAfter, 
                                       CreditTransactionType transactionType, PaymentStatus paymentStatus,
                                       String description, String referenceNumber, LocalDateTime createdAt) {
        this.id = id;
        this.creditChange = creditChange;
        this.balanceAfter = balanceAfter;
        this.transactionType = transactionType;
        this.paymentStatus = paymentStatus;
        this.description = description;
        this.referenceNumber = referenceNumber;
        this.createdAt = createdAt;
        
        computeFields();
    }

    // Additional constructor for full data
    public CreditTransactionResponseDTO(Long id, Integer creditChange, Integer balanceAfter,
                                       CreditTransactionType transactionType, PaymentStatus paymentStatus,
                                       String description, String referenceNumber, LocalDateTime createdAt,
                                       LocalDateTime processedAt, String adminNotes) {
        this.id = id;
        this.creditChange = creditChange;
        this.balanceAfter = balanceAfter;
        this.transactionType = transactionType;
        this.paymentStatus = paymentStatus;
        this.description = description;
        this.referenceNumber = referenceNumber;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
        this.adminNotes = adminNotes;
        
        computeFields();
    }

    // Helper methods
    private void computeFields() {
        if (creditChange != null) {
            this.isCreditAddition = creditChange > 0;
            this.isCreditDeduction = creditChange < 0;
        } else {
            this.isCreditAddition = false;
            this.isCreditDeduction = false;
        }
        
        if (paymentStatus != null) {
            this.isPending = paymentStatus == PaymentStatus.PENDING;
            this.isCompleted = paymentStatus == PaymentStatus.COMPLETED;
        } else {
            this.isPending = false;
            this.isCompleted = false;
        }
    }

    public boolean isSuccessful() {
        return paymentStatus == PaymentStatus.COMPLETED;
    }

    public boolean isFailed() {
        return paymentStatus == PaymentStatus.FAILED || paymentStatus == PaymentStatus.CANCELLED;
    }

    public String getAmountDisplay() {
        if (creditChange == null) return "0 credits";
        String sign = creditChange > 0 ? "+" : "";
        return sign + creditChange + " credits";
    }

    public String getStatusDisplay() {
        return paymentStatus != null ? paymentStatus.toString() : "Unknown";
    }

    public String getTypeDisplay() {
        return transactionType != null ? transactionType.toString() : "Unknown";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getCreditChange() { return creditChange; }
    public void setCreditChange(Integer creditChange) { 
        this.creditChange = creditChange; 
        computeFields();
    }
    
    public Integer getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(Integer balanceAfter) { this.balanceAfter = balanceAfter; }
    
    public CreditTransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(CreditTransactionType transactionType) { this.transactionType = transactionType; }
    
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { 
        this.paymentStatus = paymentStatus; 
        computeFields();
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public Long getCreditPackageId() { return creditPackageId; }
    public void setCreditPackageId(Long creditPackageId) { this.creditPackageId = creditPackageId; }
    
    public String getCreditPackageName() { return creditPackageName; }
    public void setCreditPackageName(String creditPackageName) { this.creditPackageName = creditPackageName; }
    
    public Long getAgencyId() { return agencyId; }
    public void setAgencyId(Long agencyId) { this.agencyId = agencyId; }
    
    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }
    
    public Boolean getIsCreditAddition() { return isCreditAddition; }
    public void setIsCreditAddition(Boolean isCreditAddition) { this.isCreditAddition = isCreditAddition; }
    
    public Boolean getIsCreditDeduction() { return isCreditDeduction; }
    public void setIsCreditDeduction(Boolean isCreditDeduction) { this.isCreditDeduction = isCreditDeduction; }
    
    public Boolean getIsPending() { return isPending; }
    public void setIsPending(Boolean isPending) { this.isPending = isPending; }
    
    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }

    @Override
    public String toString() {
        return "CreditTransactionResponseDTO{" +
                "id=" + id +
                ", creditChange=" + creditChange +
                ", transactionType=" + transactionType +
                ", paymentStatus=" + paymentStatus +
                '}';
    }
}