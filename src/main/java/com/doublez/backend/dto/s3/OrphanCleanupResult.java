package com.doublez.backend.dto.s3;

import java.time.LocalDateTime;

/**
 * Result DTO for orphan cleanup operations
 */
public class OrphanCleanupResult {
    private final int databaseImageCount;
    private final int s3ObjectCount;
    private final int deletedCount;
    private final boolean dryRun;
    private final String message;
    private final LocalDateTime timestamp;
    
    public OrphanCleanupResult(int databaseImageCount, int s3ObjectCount, 
                              int deletedCount, boolean dryRun, String message) {
        this.databaseImageCount = databaseImageCount;
        this.s3ObjectCount = s3ObjectCount;
        this.deletedCount = deletedCount;
        this.dryRun = dryRun;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public int getDatabaseImageCount() { return databaseImageCount; }
    public int getS3ObjectCount() { return s3ObjectCount; }
    public int getDeletedCount() { return deletedCount; }
    public boolean isDryRun() { return dryRun; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}