package com.doublez.backend.dto.s3;

import java.time.LocalDateTime;

/**
 * Statistics DTO for cleanup monitoring
 */
public class CleanupStats {
    private final int databaseImageCount;
    private final int s3ObjectCount;
    private final boolean dryRunEnabled;
    private final boolean cleanupEnabled;
    private final LocalDateTime timestamp;
    
    public CleanupStats(int databaseImageCount, int s3ObjectCount, 
                       boolean dryRunEnabled, boolean cleanupEnabled) {
        this.databaseImageCount = databaseImageCount;
        this.s3ObjectCount = s3ObjectCount;
        this.dryRunEnabled = dryRunEnabled;
        this.cleanupEnabled = cleanupEnabled;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public int getDatabaseImageCount() { return databaseImageCount; }
    public int getS3ObjectCount() { return s3ObjectCount; }
    public boolean isDryRunEnabled() { return dryRunEnabled; }
    public boolean isCleanupEnabled() { return cleanupEnabled; }
    public LocalDateTime getTimestamp() { return timestamp; }
}