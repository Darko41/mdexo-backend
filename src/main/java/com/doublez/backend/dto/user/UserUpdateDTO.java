package com.doublez.backend.dto.user;

import java.util.List;

//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Email;

//@JsonIgnoreProperties(ignoreUnknown = true)
public class UserUpdateDTO {
    
    @Email(message = "Invalid email format")
    private String email;
    
    private List<String> roles;
    private UserProfileDTO profile; // New field

    // Constructors
    public UserUpdateDTO() {
    }

    public UserUpdateDTO(String email, List<String> roles, UserProfileDTO profile) {
        this.email = email;
        this.roles = roles;
        this.profile = profile;
    }

    // Getters and Setters
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
}