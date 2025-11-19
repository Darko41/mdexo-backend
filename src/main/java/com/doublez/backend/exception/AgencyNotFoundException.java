package com.doublez.backend.exception;

public class AgencyNotFoundException extends RuntimeException {
    public AgencyNotFoundException(Long agencyId) {
        super("Agency not found with id: " + agencyId);
    }
    
    public AgencyNotFoundException(String message) {
        super(message);
    }
}
