package com.doublez.backend.dto.user;

import java.util.List;

import com.doublez.backend.dto.agency.AgencyCreateDTO;
import com.doublez.backend.dto.contractor.ContractorProfileCreateDTO;
import com.doublez.backend.dto.investor.InvestorProfileCreateDTO;
import com.doublez.backend.dto.owner.OwnerProfileCreateDTO;
import com.doublez.backend.entity.user.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserCreateDTO {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    // Single role for registration - users choose ONE primary role
    @NotNull
    private UserRole role = UserRole.USER;
    
    @NotBlank
    private String firstName;
    
    @NotBlank  
    private String lastName;
    
    @Pattern(regexp = "^[0-9\\s\\-\\/\\(\\)]{5,20}$")
    private String phone;
    
    private String bio;
    
    // Role-specific registration data
    private AgencyCreateDTO agency; // For AGENCY role
    private InvestorProfileCreateDTO investorProfile; // For INVESTOR role
    private OwnerProfileCreateDTO ownerProfile; // For OWNER role
    private ContractorProfileCreateDTO contractorProfile; // For CONTRACTOR role

    // Constructors
    public UserCreateDTO() {}

    public UserCreateDTO(String email, String password, UserRole role, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Helper methods
    public boolean isAgencyRegistration() {
        return role == UserRole.AGENCY && agency != null;
    }

    public boolean isInvestorRegistration() {
        return role == UserRole.INVESTOR && investorProfile != null;
    }

    public boolean isOwnerRegistration() {
        return role == UserRole.OWNER && ownerProfile != null;
    }

    public boolean isContractorRegistration() {
        return role == UserRole.CONTRACTOR && contractorProfile != null;
    }

    public boolean isBusinessRegistration() {
        return role == UserRole.BUSINESS;
    }

    public boolean isUserRegistration() {
        return role == UserRole.USER;
    }

    // Validation method to ensure role-specific data is provided
    public boolean hasRequiredRoleData() {
        switch (role) {
            case AGENCY:
                return agency != null;
            case INVESTOR:
                return investorProfile != null;
            case OWNER:
                return ownerProfile != null;
            case CONTRACTOR:
                return contractorProfile != null;
            case USER:
            case BUSINESS:
                return true; // No additional profile required
            default:
                return false;
        }
    }

    // Get the role as a list for entity conversion (your User entity expects List<String>)
    public List<String> getRolesAsList() {
        return List.of("ROLE_" + role.name());
    }

    // Validate that only the correct role-specific data is provided
    public boolean hasValidRoleDataCombination() {
        boolean hasAgencyData = agency != null;
        boolean hasInvestorData = investorProfile != null;
        boolean hasOwnerData = ownerProfile != null;
        boolean hasContractorData = contractorProfile != null;
        
        // Count how many role-specific data objects are provided
        int dataCount = 0;
        if (hasAgencyData) dataCount++;
        if (hasInvestorData) dataCount++;
        if (hasOwnerData) dataCount++;
        if (hasContractorData) dataCount++;
        
        // Should have exactly 1 role-specific data object (or 0 for USER/BUSINESS)
        if (role == UserRole.USER || role == UserRole.BUSINESS) {
            return dataCount == 0;
        } else {
            return dataCount == 1 && hasRequiredRoleData();
        }
    }

    // Get display name for the selected role
    public String getRoleDisplayName() {
        return role.getDisplayName();
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public AgencyCreateDTO getAgency() { return agency; }
    public void setAgency(AgencyCreateDTO agency) { this.agency = agency; }
    public InvestorProfileCreateDTO getInvestorProfile() { return investorProfile; }
    public void setInvestorProfile(InvestorProfileCreateDTO investorProfile) { this.investorProfile = investorProfile; }
    public OwnerProfileCreateDTO getOwnerProfile() { return ownerProfile; }
    public void setOwnerProfile(OwnerProfileCreateDTO ownerProfile) { this.ownerProfile = ownerProfile; }
    public ContractorProfileCreateDTO getContractorProfile() { return contractorProfile; }
    public void setContractorProfile(ContractorProfileCreateDTO contractorProfile) { this.contractorProfile = contractorProfile; }

    @Override
    public String toString() {
        return "UserCreateDTO{" +
                "email='" + email + '\'' +
                ", role=" + role.getDisplayName() +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", agencyRegistration=" + isAgencyRegistration() +
                '}';
    }
}