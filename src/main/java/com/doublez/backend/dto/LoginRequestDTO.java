package com.doublez.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginRequestDTO {
    
    @JsonProperty("email")  // Explicitly map JSON field
    private String email;
    
    @JsonProperty("password")  // Explicitly map JSON field  
    private String password;
    
    // REQUIRED: Default constructor
    public LoginRequestDTO() {
        System.out.println("🔄 LoginRequestDTO default constructor called");
    }
    
    // Constructor with parameters
    public LoginRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
        System.out.println("🔄 LoginRequestDTO parameterized constructor called with email: " + email);
    }
    
    // Getters and setters
    public String getEmail() {
        System.out.println("📥 getEmail() called, returning: " + email);
        return email;
    }
    
    public void setEmail(String email) {
        System.out.println("📤 setEmail() called with: " + email);
        this.email = email;
    }
    
    public String getPassword() {
        System.out.println("📥 getPassword() called, returning: [PROTECTED]");
        return password;
    }
    
    public void setPassword(String password) {
        System.out.println("📤 setPassword() called with: [PROTECTED]");
        this.password = password;
    }
    
    @Override
    public String toString() {
        return "LoginRequestDTO{email='" + email + "', password='[PROTECTED]'}";
    }
}