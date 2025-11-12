package com.doublez.backend.enums;

public enum VerificationStatus {
	
	PENDING,        // Application submitted, waiting for review
    UNDER_REVIEW,   // Currently being reviewed by admin
    APPROVED,       // Application approved
    REJECTED,       // Application rejected
    EXPIRED,        // License/verification expired
    SUSPENDED       // Temporarily suspended

}
