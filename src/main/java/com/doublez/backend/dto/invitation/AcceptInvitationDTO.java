package com.doublez.backend.dto.invitation;

public class AcceptInvitationDTO {
    
    private String token;
    private Boolean accept = true; // true = accept, false = reject
    private String message; // Optional message when rejecting
    
    // Getters and setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public Boolean getAccept() { return accept; }
    public void setAccept(Boolean accept) { this.accept = accept; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
