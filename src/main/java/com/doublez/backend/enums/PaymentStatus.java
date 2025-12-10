package com.doublez.backend.enums;

public enum PaymentStatus {
    PENDING,    // Bank transfer initiated, waiting confirmation
    COMPLETED,  // Payment confirmed by admin
    FAILED,     // Payment rejected or failed
    REFUNDED,   // Credits refunded
    CANCELLED   // Payment cancelled by user
}