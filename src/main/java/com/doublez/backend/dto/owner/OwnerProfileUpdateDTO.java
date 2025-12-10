package com.doublez.backend.dto.owner;

public class OwnerProfileUpdateDTO {
    private String propertyOwnershipDocs;
    private String idDocumentNumber;
    private String idDocumentType;
    private String taxNumber;
    private String bankAccountNumber;
    private String preferredContactMethod;
    private String contactHours;

    // Constructors
    public OwnerProfileUpdateDTO() {}

    // Helper methods
    public boolean hasUpdates() {
        return propertyOwnershipDocs != null || idDocumentNumber != null || idDocumentType != null ||
               taxNumber != null || bankAccountNumber != null || preferredContactMethod != null ||
               contactHours != null;
    }

    public boolean hasDocumentUpdates() {
        return propertyOwnershipDocs != null || idDocumentNumber != null || idDocumentType != null;
    }

    public boolean hasFinancialUpdates() {
        return taxNumber != null || bankAccountNumber != null;
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
