package com.doublez.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.VerificationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "owner_profiles")
public class OwnerProfile {

    @Id
    private Long id;

    @Column(name = "property_ownership_docs")
    private String propertyOwnershipDocs; // JSON or comma-separated document references

    @Column(name = "id_document_number")
    private String idDocumentNumber;

    @Column(name = "id_document_type")
    private String idDocumentType; // "LICNA_KARTA", "PASOS", "VODJICA"

    @Column(name = "tax_number")
    private String taxNumber;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "preferred_contact_method")
    private String preferredContactMethod; // "PHONE", "EMAIL", "BOTH"

    @Column(name = "contact_hours")
    private String contactHours; // "9-17", "flexible", "weekends_only"

    @Column(name = "properties_owned")
    private Integer propertiesOwned = 0;

    @Column(name = "properties_listed")
    private Integer propertiesListed = 0;

    @Column(name = "properties_sold")
    private Integer propertiesSold = 0;

    @Column(name = "verification_status")
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verification_notes")
    private String verificationNotes;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    // ========================
    // CONSTRUCTORS
    // ========================

    public OwnerProfile() {
    }

    public OwnerProfile(User user) {
        this.user = user;
    }

    // ========================
    // BUSINESS METHODS
    // ========================

    public void incrementPropertiesOwned() {
        this.propertiesOwned = (this.propertiesOwned == null) ? 1 : this.propertiesOwned + 1;
    }

    public void incrementPropertiesListed() {
        this.propertiesListed = (this.propertiesListed == null) ? 1 : this.propertiesListed + 1;
    }

    public void incrementPropertiesSold() {
        this.propertiesSold = (this.propertiesSold == null) ? 1 : this.propertiesSold + 1;
    }

    public void verify() {
        this.verificationStatus = VerificationStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }

    public void rejectVerification(String notes) {
        this.verificationStatus = VerificationStatus.REJECTED;
        this.verificationNotes = notes;
    }

    public boolean isVerified() {
        return verificationStatus == VerificationStatus.VERIFIED;
    }

    public boolean canListProperties() {
        return isVerified() && propertiesListed < 3; // 3 free listings for owners
    }

    public List<String> getOwnershipDocsList() {
        if (propertyOwnershipDocs == null || propertyOwnershipDocs.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(propertyOwnershipDocs.split(","));
    }

    // ========================
    // GETTERS AND SETTERS
    // ========================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    @Override
    public String toString() {
        return "OwnerProfile{" +
                "id=" + id +
                ", verificationStatus=" + verificationStatus +
                ", propertiesOwned=" + propertiesOwned +
                ", propertiesListed=" + propertiesListed +
                '}';
    }
}
