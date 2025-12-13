package com.doublez.backend.enums.warnings;

public enum WarningStatus {
    ACTIVE,         // Warning detected, not addressed
    ACKNOWLEDGED,   // User saw it but not fixed
    RESOLVED,       // Issue fixed
    DISMISSED,      // User dismissed (false alarm)
    EXPIRED         // Auto-resolved by time
}
