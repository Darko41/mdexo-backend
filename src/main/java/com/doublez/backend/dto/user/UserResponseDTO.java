package com.doublez.backend.dto.user;

import java.time.LocalDate;
import java.util.List;

public class UserResponseDTO {
    private Long id;
    private String email;
    private List<String> roles;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private UserProfileDTO profile;

    // Constructors
    public UserResponseDTO() {
    }

    public UserResponseDTO(Long id, String email, List<String> roles, 
                          LocalDate createdAt, LocalDate updatedAt) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UserResponseDTO(Long id, String email, List<String> roles,
                          LocalDate createdAt, LocalDate updatedAt,
                          UserProfileDTO profile) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.profile = profile;
    }

    // Getters and Setters
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
}