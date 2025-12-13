package com.doublez.backend.enums.notifications;

public enum NotificationStatus {
    PENDING,        // Waiting to be sent
    SENT,           // Attempted delivery
    DELIVERED,      // Confirmed delivery
    READ,           // User opened/read it
    FAILED,         // Delivery failed
    BOUNCED         // Email bounced
}
