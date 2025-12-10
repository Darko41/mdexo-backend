package com.doublez.backend.dto.owner;

import java.time.LocalDateTime;
import java.util.List;

import com.doublez.backend.dto.user.UserResponseDTO;
import com.doublez.backend.enums.VerificationStatus;

public class OwnerProfileResponseDTO {
    private Long id;
    private String propertyOwnershipDocs;
    private List<String> ownershipDocsList;
    private String idDocumentNumber;
    private String idDocumentType;
    private String taxNumber;
    private String bankAccountNumber;
    private String preferredContactMethod;
    private String contactHours;
    private Integer propertiesOwned;
    private Integer propertiesListed;
    private Integer propertiesSold;
    private VerificationStatus verificationStatus;
    private LocalDateTime verifiedAt;
    private String verificationNotes;
    private UserResponseDTO user;

    // Constructors
    public OwnerProfileResponseDTO() {}

    public OwnerProfileResponseDTO(Long id, String propertyOwnershipDocs, String idDocumentNumber, 
                                  String idDocumentType, String taxNumber, String bankAccountNumber,
                                  String preferredContactMethod, String contactHours) {
        this.id = id;
        this.propertyOwnershipDocs = propertyOwnershipDocs;
        this.idDocumentNumber = idDocumentNumber;
        this.idDocumentType = idDocumentType;
        this.taxNumber = taxNumber;
        this.bankAccountNumber = bankAccountNumber;
        this.preferredContactMethod = preferredContactMethod;
        this.contactHours = contactHours;
    }

    // Helper methods
    public boolean isVerified() {
        return verificationStatus == VerificationStatus.VERIFIED;
    }

    public boolean canListProperties() {
        return isVerified() && (propertiesListed == null || propertiesListed < 3); // 3 free listings
    }

    public boolean hasOwnershipDocuments() {
        return propertyOwnershipDocs != null && !propertyOwnershipDocs.trim().isEmpty();
    }

    public boolean hasFinancialInfo() {
        return taxNumber != null && !taxNumber.trim().isEmpty();
    }

    public boolean hasBankInfo() {
        return bankAccountNumber != null && !bankAccountNumber.trim().isEmpty();
    }

    public Integer getRemainingFreeListings() {
        if (propertiesListed == null) return 3;
        return Math.max(0, 3 - propertiesListed);
    }

    public String getVerificationStatusDisplay() {
        if (verificationStatus == null) return "Not Submitted";
        return verificationStatus.toString();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPropertyOwnershipDocs() { return propertyOwnershipDocs; }
    public void setPropertyOwnershipDocs(String propertyOwnershipDocs) { this.propertyOwnershipDocs = propertyOwnershipDocs; }
    public List<String> getOwnershipDocsList() { return ownershipDocsList; }
    public void setOwnershipDocsList(List<String> ownershipDocsList) { this.ownershipDocsList = ownershipDocsList; }
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
    public Integer getPropertiesOwned() { return propertiesOwned; }
    public void setPropertiesOwned(Integer propertiesOwned) { this.propertiesOwned = propertiesOwned; }
    public Integer getPropertiesListed() { return propertiesListed; }
    public void setPropertiesListed(Integer propertiesListed) { this.propertiesListed = propertiesListed; }
    public Integer getPropertiesSold() { return propertiesSold; }
    public void setPropertiesSold(Integer propertiesSold) { this.propertiesSold = propertiesSold; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }
    public UserResponseDTO getUser() { return user; }
    public void setUser(UserResponseDTO user) { this.user = user; }
}
