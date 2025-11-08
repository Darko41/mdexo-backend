package com.doublez.backend.exception.agent;

public class AgencyNotFoundException extends RuntimeException {
    public AgencyNotFoundException(Long agencyId) {
        super("Agency not found with id: " + agencyId);
    }
}
