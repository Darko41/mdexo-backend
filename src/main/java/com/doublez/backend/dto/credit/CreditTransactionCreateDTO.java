package com.doublez.backend.dto.credit;

import jakarta.validation.constraints.NotNull;

public class CreditTransactionCreateDTO {
    @NotNull
    private Long creditPackageId;
    
    private String referenceNumber; // For bank transfers
    private String description;
    private String metadata; // JSON for additional data

    // Constructors
    public CreditTransactionCreateDTO() {}

    public CreditTransactionCreateDTO(Long creditPackageId, String referenceNumber) {
        this.creditPackageId = creditPackageId;
        this.referenceNumber = referenceNumber;
    }

    public CreditTransactionCreateDTO(Long creditPackageId, String referenceNumber, String description) {
        this.creditPackageId = creditPackageId;
        this.referenceNumber = referenceNumber;
        this.description = description;
    }

    // Helper methods
    public boolean isBankTransfer() {
        return referenceNumber != null && !referenceNumber.trim().isEmpty();
    }

    // Getters and Setters
    public Long getCreditPackageId() { return creditPackageId; }
    public void setCreditPackageId(Long creditPackageId) { this.creditPackageId = creditPackageId; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    @Override
    public String toString() {
        return "CreditTransactionCreateDTO{" +
                "creditPackageId=" + creditPackageId +
                ", referenceNumber='" + referenceNumber + '\'' +
                ", isBankTransfer=" + isBankTransfer() +
                '}';
    }
}
