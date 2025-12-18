package com.doublez.backend.service.s3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.doublez.backend.dto.s3.CleanupStats;
import com.doublez.backend.dto.s3.OrphanCleanupResult;
import com.doublez.backend.repository.realestate.RealEstateRepository;
import com.doublez.backend.service.cloudfront.CloudFrontService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class S3OrphanCleanupService {
    private static final Logger logger = LoggerFactory.getLogger(S3OrphanCleanupService.class);
    
    private final S3Service s3Service;
    private final RealEstateRepository realEstateRepository;
    private final CloudFrontService cloudFrontService;
    
    @Value("${app.s3.folder:real-estates}")
    private String s3Folder;
    
    @Value("${app.s3.cleanup.enabled:true}")
    private boolean cleanupEnabled;
    
    @Value("${app.s3.cleanup.dry-run:false}")
    private boolean dryRun;

    public S3OrphanCleanupService(S3Service s3Service, RealEstateRepository realEstateRepository, CloudFrontService cloudFrontService) {
        this.s3Service = s3Service;
        this.realEstateRepository = realEstateRepository;
        this.cloudFrontService = cloudFrontService;
    }
    
    /**
     * Scheduled cleanup - runs every Sunday at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * SUN") // Every Sunday at 2 AM
    public void scheduledOrphanCleanup() {
        if (!cleanupEnabled) {
            logger.info("🔄 Orphan cleanup is disabled via configuration");
            return;
        }
        
        logger.info("🔄 Starting scheduled orphaned S3 image cleanup...");
        cleanupOrphanedImages();
    }
    
    /**
     * Manual trigger for orphan cleanup
     */
    public OrphanCleanupResult cleanupOrphanedImages() {
        if (!cleanupEnabled) {
            logger.info("🔄 Orphan cleanup is disabled via configuration");
            return new OrphanCleanupResult(0, 0, 0, true, "Cleanup disabled");
        }
        
        logger.info("🔍 Starting orphaned S3 image cleanup (Dry Run: {})...", dryRun);
        
        try {
            // 1. Get all image URLs from database
            Set<String> allDbImageUrls = getAllDatabaseImageUrls();
            logger.info("📊 Found {} image references in database", allDbImageUrls.size());
            
            // 2. List all files in S3 bucket
            Set<String> allS3Objects = listAllS3Objects();
            logger.info("📊 Found {} objects in S3 bucket", allS3Objects.size());
            
            // 3. Find orphans (in S3 but not in database)
            Set<String> orphanedImages = findOrphanedImages(allDbImageUrls, allS3Objects);
            
            logger.info("📊 Analysis: {} orphaned images found", orphanedImages.size());
            
            // 4. Delete orphans (unless dry run)
            int deletedCount = 0;
            if (!orphanedImages.isEmpty() && !dryRun) {
                deletedCount = deleteOrphanedImages(orphanedImages);
                logger.info("✅ Orphan cleanup completed - deleted {} files", deletedCount);
            } else if (!orphanedImages.isEmpty()) {
                logger.info("🧪 Dry Run: Would delete {} orphaned images", orphanedImages.size());
            } else {
                logger.info("✅ No orphaned images found");
            }
            
            return new OrphanCleanupResult(
                allDbImageUrls.size(), 
                allS3Objects.size(), 
                deletedCount, 
                dryRun,
                dryRun ? "Dry run completed" : "Cleanup completed"
            );
            
        } catch (Exception e) {
            logger.error("❌ Orphan cleanup failed: {}", e.getMessage(), e);
            return new OrphanCleanupResult(0, 0, 0, dryRun, "Cleanup failed: " + e.getMessage());
        }
    }
    
    /**
     * Get all image URLs from database
     */
    private Set<String> getAllDatabaseImageUrls() {
        return realEstateRepository.findAll().stream()
            .flatMap(property -> {
                List<String> images = property.getImages();
                // 🆕 Explicitly handle null and return Stream
                if (images == null || images.isEmpty()) {
                    return Stream.empty();
                }
                return images.stream();
            })
            .filter(url -> url != null && !url.trim().isEmpty())
            .collect(Collectors.toSet());
    }
    
    /**
     * List all objects in S3 bucket (with pagination support)
     */
    private Set<String> listAllS3Objects() {
        try {
            // List objects with the real-estates prefix (or your configured folder)
            List<String> s3Objects = s3Service.listObjects(s3Folder + "/");
            logger.info("📊 Successfully listed {} objects from S3", s3Objects.size());
            return new HashSet<>(s3Objects);
        } catch (Exception e) {
            logger.error("❌ Failed to list S3 objects: {}", e.getMessage());
            return Collections.emptySet();
        }
    }
    
    /**
     * Find orphaned images (in S3 but not in database)
     */
    private Set<String> findOrphanedImages(Set<String> dbImageUrls, Set<String> s3Objects) {
        return s3Objects.stream()
            .filter(s3Key -> {
                // Extract the public URL format that matches what's stored in database
                String imageUrl = convertS3KeyToUrl(s3Key);
                return !dbImageUrls.contains(imageUrl);
            })
            .collect(Collectors.toSet());
    }
    
    /**
     * Convert S3 key to URL format (matches what's stored in database)
     */
    private String convertS3KeyToUrl(String s3Key) {
        // Build S3 URL first, then convert to CDN
        String s3Url = "https://mdexoawsbucket.s3.eu-north-1.amazonaws.com/" + s3Key;
        return cloudFrontService.convertToCdnUrl(s3Url);
    }
    
    /**
     * Delete orphaned images from S3
     */
    private int deleteOrphanedImages(Set<String> orphanedImages) {
        if (orphanedImages.isEmpty()) {
            return 0;
        }
        
        logger.warn("🗑️ Deleting {} orphaned images from S3", orphanedImages.size());
        
        int successCount = 0;
        int failureCount = 0;
        List<String> failedDeletions = new ArrayList<>();
        
        for (String s3Key : orphanedImages) {
            try {
                if (!dryRun) {
                    s3Service.deleteFile(s3Key);
                }
                successCount++;
                logger.debug("✅ Deleted orphaned image: {}", s3Key);
            } catch (Exception e) {
                failureCount++;
                failedDeletions.add(s3Key);
                logger.error("❌ Failed to delete orphaned image {}: {}", s3Key, e.getMessage());
            }
        }
        
        if (failureCount > 0) {
            logger.error("❌ Failed to delete {} orphaned images: {}", failureCount, failedDeletions);
        }
        
        logger.info("✅ Successfully deleted {} orphaned images from S3", successCount);
        return successCount;
    }
    
    /**
     * Get cleanup statistics (for monitoring/API)
     */
    public CleanupStats getCleanupStats() {
        Set<String> dbImageUrls = getAllDatabaseImageUrls();
        // Note: S3 object count would require actual S3 listing implementation
        return new CleanupStats(dbImageUrls.size(), 0, dryRun, cleanupEnabled);
    }
}