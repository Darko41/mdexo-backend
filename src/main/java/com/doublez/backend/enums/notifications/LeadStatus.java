package com.doublez.backend.enums.notifications;

public enum LeadStatus {
    NEW,            // Just received
    OPENED,         // Agent opened it
    CONTACTED,      // First response sent
    FOLLOW_UP,      // In follow-up process
    CONVERTED,      // Became a client/deal
    LOST,           // Not interested
    ARCHIVED        // Old/irrelevant
}
