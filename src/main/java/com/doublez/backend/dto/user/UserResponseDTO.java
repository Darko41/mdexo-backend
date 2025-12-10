package com.doublez.backend.dto.user;

import java.time.LocalDateTime;
import java.util.List;

import com.doublez.backend.dto.agency.AgencyInfoDTO;
import com.doublez.backend.dto.contractor.ContractorProfileResponseDTO;
import com.doublez.backend.dto.credit.CreditBalanceDTO;
import com.doublez.backend.dto.investor.InvestorProfileResponseDTO;
import com.doublez.backend.dto.owner.OwnerProfileResponseDTO;
import com.doublez.backend.dto.trial.TrialInfoDTO;
import com.doublez.backend.entity.user.UserTier;

public class UserResponseDTO {
    private Long id;
    private String email;
    private List<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserProfileDTO profile;
    
    // Agency Administration (only for ROLE_AGENCY_ADMIN)
    // Represents the Agency entity this user owns and manages
    private AgencyInfoDTO administeredAgency;
    
    // Role-specific profiles
    private InvestorProfileResponseDTO investorProfile;
    private OwnerProfileResponseDTO ownerProfile;
    private ContractorProfileResponseDTO contractorProfile;
    
    // Trial information
    private TrialInfoDTO trialInfo;
    
    // Credit information
    private CreditBalanceDTO creditBalance;

    // Constructors
    public UserResponseDTO() {}

    public UserResponseDTO(Long id, String email, List<String> roles, LocalDateTime createdAt, 
                          LocalDateTime updatedAt, UserProfileDTO profile) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.profile = profile;
    }

    // Helper methods
    public boolean isAgencyAdmin() {
        return roles != null && roles.contains("ROLE_AGENCY_ADMIN");
    }

    public boolean isInvestor() {
        return roles != null && roles.contains("ROLE_INVESTOR");
    }

    public boolean isOwner() {
        return roles != null && roles.contains("ROLE_OWNER");
    }

    public boolean isContractor() {
        return roles != null && roles.contains("ROLE_CONTRACTOR");
    }

    public boolean isBusiness() {
        return roles != null && roles.contains("ROLE_BUSINESS");
    }

    public boolean isRegularUser() {
        return roles != null && roles.contains("ROLE_USER");
    }

    public boolean hasAdministeredAgency() {
        return isAgencyAdmin() && administeredAgency != null;
    }

    public boolean hasInvestorProfile() {
        return isInvestor() && investorProfile != null;
    }

    public boolean hasOwnerProfile() {
        return isOwner() && ownerProfile != null;
    }

    public boolean hasContractorProfile() {
        return isContractor() && contractorProfile != null;
    }

    public boolean hasTrial() {
        return trialInfo != null && trialInfo.getTrialUsed() != null && trialInfo.getTrialUsed();
    }

    public boolean isInTrialPeriod() {
        return trialInfo != null && Boolean.TRUE.equals(trialInfo.getInTrialPeriod());
    }

    public boolean hasCreditBalance() {
        return creditBalance != null && creditBalance.getCurrentBalance() != null;
    }

    public String getPrimaryRoleDisplay() {
        if (roles == null || roles.isEmpty()) {
            return "No Role";
        }
        
        String primaryRole = roles.get(0);
        switch (primaryRole) {
            case "ROLE_USER": return "User";
            case "ROLE_OWNER": return "Property Owner";
            case "ROLE_BUSINESS": return "Business";
            case "ROLE_AGENCY_ADMIN": return "Agency Administrator";
            case "ROLE_INVESTOR": return "Investor";
            case "ROLE_CONTRACTOR": return "Contractor";
            default: return primaryRole.replace("ROLE_", "");
        }
    }

    public String getFullName() {
        if (profile != null && profile.getFirstName() != null && profile.getLastName() != null) {
            return profile.getFirstName() + " " + profile.getLastName();
        }
        return "Name not set";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public UserProfileDTO getProfile() { return profile; }
    public void setProfile(UserProfileDTO profile) { this.profile = profile; }
    public AgencyInfoDTO getAdministeredAgency() { return administeredAgency; }
    public void setAdministeredAgency(AgencyInfoDTO administeredAgency) { this.administeredAgency = administeredAgency; }
    public InvestorProfileResponseDTO getInvestorProfile() { return investorProfile; }
    public void setInvestorProfile(InvestorProfileResponseDTO investorProfile) { this.investorProfile = investorProfile; }
    public OwnerProfileResponseDTO getOwnerProfile() { return ownerProfile; }
    public void setOwnerProfile(OwnerProfileResponseDTO ownerProfile) { this.ownerProfile = ownerProfile; }
    public ContractorProfileResponseDTO getContractorProfile() { return contractorProfile; }
    public void setContractorProfile(ContractorProfileResponseDTO contractorProfile) { this.contractorProfile = contractorProfile; }
    public TrialInfoDTO getTrialInfo() { return trialInfo; }
    public void setTrialInfo(TrialInfoDTO trialInfo) { this.trialInfo = trialInfo; }
    public CreditBalanceDTO getCreditBalance() { return creditBalance; }
    public void setCreditBalance(CreditBalanceDTO creditBalance) { this.creditBalance = creditBalance; }

    @Override
    public String toString() {
        return "UserResponseDTO{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                ", primaryRole='" + getPrimaryRoleDisplay() + '\'' +
                ", hasAdministeredAgency=" + hasAdministeredAgency() +
                '}';
    }
}