package com.doublez.backend.dto.credit;

import java.math.BigDecimal;

import com.doublez.backend.enums.CreditTransactionType;

import jakarta.validation.constraints.NotNull;

public class CreditTransactionCreateDTO {
    @NotNull
    private Long creditPackageId;
    
    // User or Agency reference
    private Long userId;          // For individual purchases
    private Long agencyId;        // For agency purchases
    
    // Transaction details
    private Integer creditChange; // Positive for purchase, negative for usage
    private CreditTransactionType transactionType; // PURCHASE, USAGE, REFUND, etc.
    
    private String referenceNumber; // For bank transfers
    private String description;
    private String metadata; // JSON for additional data
    
    private BigDecimal totalAmount; // Purchase amount
    private String paymentMethod; // "BANK_TRANSFER", "CREDIT_CARD", "FREE"
    
    // Constructors
    public CreditTransactionCreateDTO() {}

    // Constructor for simple individual purchase
    public CreditTransactionCreateDTO(Long userId, Long creditPackageId) {
        this.userId = userId;
        this.creditPackageId = creditPackageId;
    }

    // Constructor for agency purchase
    public CreditTransactionCreateDTO(Long agencyId, Long creditPackageId, Integer creditChange) {
        this.agencyId = agencyId;
        this.creditPackageId = creditPackageId;
        this.creditChange = creditChange;
    }

    // Helper methods
    public boolean isBankTransfer() {
        return "BANK_TRANSFER".equals(paymentMethod) || 
               (referenceNumber != null && !referenceNumber.trim().isEmpty());
    }
    
    public boolean isAgencyPurchase() {
        return agencyId != null;
    }
    
    public boolean isIndividualPurchase() {
        return userId != null && agencyId == null;
    }

    // Getters and Setters
    public Long getCreditPackageId() { return creditPackageId; }
    public void setCreditPackageId(Long creditPackageId) { this.creditPackageId = creditPackageId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Long getAgencyId() { return agencyId; }
    public void setAgencyId(Long agencyId) { this.agencyId = agencyId; }
    
    public Integer getCreditChange() { return creditChange; }
    public void setCreditChange(Integer creditChange) { this.creditChange = creditChange; }
    
    public CreditTransactionType  getTransactionType() { return transactionType; }
    public void setTransactionType(CreditTransactionType  transactionType) { this.transactionType = transactionType; }
    
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    @Override
    public String toString() {
        return "CreditTransactionCreateDTO{" +
                "creditPackageId=" + creditPackageId +
                ", userId=" + userId +
                ", agencyId=" + agencyId +
                ", creditChange=" + creditChange +
                ", isBankTransfer=" + isBankTransfer() +
                ", isAgencyPurchase=" + isAgencyPurchase() +
                '}';
    }
}
