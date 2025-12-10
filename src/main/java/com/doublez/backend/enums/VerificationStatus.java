package com.doublez.backend.enums;

public enum VerificationStatus {
    PENDING,        // Created but not submitted for verification
    REQUESTED,      // Agency requested verification
    SUBMITTED,      // Documents submitted, waiting review
    UNDER_REVIEW,   // Admin is reviewing documents
    VERIFIED,       // Successfully verified
    REJECTED,       // Verification failed
    SUSPENDED       // Previously verified but suspended
}
