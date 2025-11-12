package com.doublez.backend.dto.user;

import java.time.LocalDate;
import java.util.List;

import com.doublez.backend.dto.InvestorProfileDTO;
import com.doublez.backend.dto.agent.AgencyInfoDTO;
import com.doublez.backend.enums.UserTier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDTO {
    private Long id;

    @NotBlank
    @Email
    private String email;

    private List<String> roles;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private UserProfileDTO profile;
    private AgencyInfoDTO currentAgency;
    
    // 🆕 ADD THESE FIELDS
    private UserTier tier;
    private InvestorProfileDTO investorProfile;

    public static class Create {
        @NotBlank
        @Email
        private String email;

        @NotBlank
        @Size(min = 6)
        private String password;

        private List<@NotBlank String> roles = List.of("ROLE_USER");
        private UserProfileDTO profile;
        private UserTier tier = UserTier.FREE_USER; // 🆕 ADD DEFAULT TIER

        // Getters and setters for Create class
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }

        public UserProfileDTO getProfile() {
            return profile;
        }

        public void setProfile(UserProfileDTO profile) {
            this.profile = profile;
        }
        
        // 🆕 ADD GETTER/SETTER FOR TIER
        public UserTier getTier() {
            return tier;
        }

        public void setTier(UserTier tier) {
            this.tier = tier;
        }
    }

    public static class Update {
        @Email
        private String email;
        private List<String> roles;
        private UserProfileDTO profile;
        private UserTier tier; // 🆕 ADD TIER

        // Getters and setters for Update class
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }

        public UserProfileDTO getProfile() {
            return profile;
        }

        public void setProfile(UserProfileDTO profile) {
            this.profile = profile;
        }
        
        // 🆕 ADD GETTER/SETTER FOR TIER
        public UserTier getTier() {
            return tier;
        }

        public void setTier(UserTier tier) {
            this.tier = tier;
        }
    }

    public UserDTO() {
    }

    // 🆕 UPDATED CONSTRUCTOR
    public UserDTO(Long id, String email, List<String> roles, LocalDate createdAt, LocalDate updatedAt,
            UserProfileDTO profile, AgencyInfoDTO currentAgency, UserTier tier, InvestorProfileDTO investorProfile) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.profile = profile;
        this.currentAgency = currentAgency;
        this.tier = tier;
        this.investorProfile = investorProfile;
    }

    // Getters and setters for main UserDTO
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UserProfileDTO getProfile() {
        return profile;
    }

    public void setProfile(UserProfileDTO profile) {
        this.profile = profile;
    }

    public AgencyInfoDTO getCurrentAgency() {
        return currentAgency;
    }

    public void setCurrentAgency(AgencyInfoDTO currentAgency) {
        this.currentAgency = currentAgency;
    }
    
    public UserTier getTier() {
        return tier;
    }

    public void setTier(UserTier tier) {
        this.tier = tier;
    }

    public InvestorProfileDTO getInvestorProfile() {
        return investorProfile;
    }

    public void setInvestorProfile(InvestorProfileDTO investorProfile) {
        this.investorProfile = investorProfile;
    }
}