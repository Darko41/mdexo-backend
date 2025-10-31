package com.doublez.backend.dto.user;

import jakarta.validation.constraints.Pattern;

public class UserProfileDTO {
    private String firstName;
    private String lastName;
    
    @Pattern(regexp = "^[0-9\\s\\-\\/\\(\\)]{5,20}$", message = "Invalid phone number format")
    private String phone;
    
    private String bio;

    // Constructors
    public UserProfileDTO() {
    }

    public UserProfileDTO(String firstName, String lastName, String phone, String bio) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.bio = bio;
    }

    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
