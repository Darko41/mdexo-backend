package com.doublez.backend.dto.agent;

import java.time.LocalDate;
import java.util.List;

import com.doublez.backend.dto.user.UserDTO;

import jakarta.validation.constraints.NotBlank;

public class AgencyDTO {
    private Long id;
    private String name;
    private String description;
    private String logo;
    private String contactInfo;
    private UserDTO admin;
    private List<AgencyMembershipDTO> memberships;
    private LocalDate createdAt;

    public static class Create {
        @NotBlank
        private String name;
        
        private String description;
        private String logo;
        private String contactInfo;

        // Default constructor (REQUIRED for Jackson)
        public Create() {}

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getLogo() { return logo; }
        public void setLogo(String logo) { this.logo = logo; }
        public String getContactInfo() { return contactInfo; }
        public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    }

    // Getters and setters for main class
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    public UserDTO getAdmin() { return admin; }
    public void setAdmin(UserDTO admin) { this.admin = admin; }
    public List<AgencyMembershipDTO> getMemberships() { return memberships; }
    public void setMemberships(List<AgencyMembershipDTO> memberships) { this.memberships = memberships; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
}
