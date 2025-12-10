package com.doublez.backend.dto.owner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OwnerProfileCreateDTO {
    private String propertyOwnershipDocs; // JSON or comma-separated document references
    private String idDocumentNumber;
    private String idDocumentType; // "LICNA_KARTA", "PASOS", "VODJICA"
    private String taxNumber;
    private String bankAccountNumber;
    private String preferredContactMethod; // "PHONE", "EMAIL", "BOTH"
    private String contactHours; // "9-17", "flexible", "weekends_only"

    // Constructors
    public OwnerProfileCreateDTO() {}

    public OwnerProfileCreateDTO(String propertyOwnershipDocs, String idDocumentNumber, String idDocumentType, 
                                String taxNumber, String bankAccountNumber, String preferredContactMethod, 
                                String contactHours) {
        this.propertyOwnershipDocs = propertyOwnershipDocs;
        this.idDocumentNumber = idDocumentNumber;
        this.idDocumentType = idDocumentType;
        this.taxNumber = taxNumber;
        this.bankAccountNumber = bankAccountNumber;
        this.preferredContactMethod = preferredContactMethod;
        this.contactHours = contactHours;
    }

    // Helper methods
    public boolean hasVerificationDocuments() {
        return (propertyOwnershipDocs != null && !propertyOwnershipDocs.trim().isEmpty()) &&
               (idDocumentNumber != null && !idDocumentNumber.trim().isEmpty());
    }

    public boolean hasFinancialInfo() {
        return taxNumber != null && !taxNumber.trim().isEmpty();
    }

    public boolean hasBankInfo() {
        return bankAccountNumber != null && !bankAccountNumber.trim().isEmpty();
    }

    public List<String> getOwnershipDocsList() {
        if (propertyOwnershipDocs == null || propertyOwnershipDocs.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(propertyOwnershipDocs.split(","));
    }

    // Getters and Setters
    public String getPropertyOwnershipDocs() { return propertyOwnershipDocs; }
    public void setPropertyOwnershipDocs(String propertyOwnershipDocs) { this.propertyOwnershipDocs = propertyOwnershipDocs; }
    public String getIdDocumentNumber() { return idDocumentNumber; }
    public void setIdDocumentNumber(String idDocumentNumber) { this.idDocumentNumber = idDocumentNumber; }
    public String getIdDocumentType() { return idDocumentType; }
    public void setIdDocumentType(String idDocumentType) { this.idDocumentType = idDocumentType; }
    public String getTaxNumber() { return taxNumber; }
    public void setTaxNumber(String taxNumber) { this.taxNumber = taxNumber; }
    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }
    public String getPreferredContactMethod() { return preferredContactMethod; }
    public void setPreferredContactMethod(String preferredContactMethod) { this.preferredContactMethod = preferredContactMethod; }
    public String getContactHours() { return contactHours; }
    public void setContactHours(String contactHours) { this.contactHours = contactHours; }
}
