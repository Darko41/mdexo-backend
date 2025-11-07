package com.doublez.backend.service.realestate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.doublez.backend.exception.image.ImageUploadException;
import com.doublez.backend.service.image.ImageProcessingService;
import com.doublez.backend.service.s3.RealS3Service;
import com.doublez.backend.service.s3.S3Service;
import com.doublez.backend.service.validation.FileValidationService;

import jakarta.transaction.Transactional;

@Service
public class RealEstateImageService {
    private static final Logger logger = LoggerFactory.getLogger(RealEstateImageService.class);
    
    private final S3Service s3Service;
    private final ImageProcessingService imageProcessingService;
    private final FileValidationService validationService;
    
    @Value("${app.s3.folder:real-estates}")
    private String s3Folder;

    public RealEstateImageService(S3Service s3Service, 
                                 ImageProcessingService imageProcessingService,
                                 FileValidationService validationService) {
        this.s3Service = s3Service;
        this.imageProcessingService = imageProcessingService;
        this.validationService = validationService;
    }

    // Sequential upload with processing - MEMORY SAFE
    public List<String> uploadRealEstateImages(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }

        validationService.validateFiles(files);
        List<String> imageUrls = new ArrayList<>();

        logger.info("🔄 Starting smart processing of {} images", files.length);

        // Sort files by size (largest first) to process biggest memory hogs first
        List<MultipartFile> sortedFiles = Arrays.stream(files)
            .filter(file -> !file.isEmpty())
            .sorted((f1, f2) -> Long.compare(f2.getSize(), f1.getSize())) // Descending by size
            .collect(Collectors.toList());

        int processedCount = 0;
        int skippedCount = 0;
        
        for (MultipartFile file : sortedFiles) {
            try {
                long fileSizeMB = file.getSize() / (1024 * 1024);
                
                // Smart decision: Only process large images (>2MB) and when we have enough memory
                boolean shouldProcess = shouldProcessImage(file, processedCount);
                
                if (shouldProcess) {
                    logger.info("📦 Processing large image {}/{}: {} ({} MB)", 
                        processedCount + 1, sortedFiles.size(), file.getOriginalFilename(),
                        String.format("%.1f", fileSizeMB));

                    String imageUrl = uploadAndProcessImage(file);
                    imageUrls.add(imageUrl);
                    processedCount++;
                    
                    logger.info("✅ Successfully processed image {}/{}", processedCount, sortedFiles.size());
                    
                    // Memory management
                    if (processedCount < sortedFiles.size()) {
                        performMemoryCleanup();
                    }
                    
                } else {
                    // Upload as original
                    logger.info("📤 Skipping processing for image {} ({} MB) - uploading original", 
                        file.getOriginalFilename(), String.format("%.1f", fileSizeMB));
                    
                    String imageUrl = uploadOriginalImage(file);
                    imageUrls.add(imageUrl);
                    skippedCount++;
                }
                
            } catch (Exception e) {
                logger.error("❌ Failed to process image {}: {}", file.getOriginalFilename(), e.getMessage());
                
                // Fallback: upload as original
                try {
                    String imageUrl = uploadOriginalImage(file);
                    imageUrls.add(imageUrl);
                    skippedCount++;
                    logger.info("🔄 Uploaded original after failure: {}", file.getOriginalFilename());
                } catch (Exception ex) {
                    logger.error("❌ Failed to upload original image: {}", file.getOriginalFilename());
                }
            }
        }

        logger.info("🎉 Completed: {} images processed, {} uploaded as original", processedCount, skippedCount);
        return imageUrls;
    }
    
    private boolean shouldProcessImage(MultipartFile file, int alreadyProcessed) {
        long fileSize = file.getSize();
        long fileSizeMB = fileSize / (1024 * 1024L);
        
        // Rule 1: Only process images larger than 1MB (small ones don't need much compression)
        if (fileSizeMB < 1) {
            logger.debug("🔄 Skip processing - image too small: {} MB", fileSizeMB);
            return false;
        }
        
        // Rule 2: Check current memory availability
        if (!hasSufficientMemoryForProcessing()) {
            logger.debug("🔄 Skip processing - insufficient memory");
            return false;
        }
        
        // Rule 3: Limit number of processed images based on memory conditions
        int maxProcessable = calculateMaxProcessableImages();
        if (alreadyProcessed >= maxProcessable) {
            logger.debug("🔄 Skip processing - reached max processable limit: {}", maxProcessable);
            return false;
        }
        
        // Rule 4: For very large files (>5MB), be more conservative
        if (fileSizeMB > 5 && alreadyProcessed > 2) {
            logger.debug("🔄 Skip processing - large file after several processed");
            return false;
        }
        
        return true;
    }
    
    private boolean hasSufficientMemoryForProcessing() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long availableMemory = maxMemory - usedMemory;
        
        // Require at least 40MB available for safe processing
        long requiredMemory = 40 * 1024 * 1024L;
        boolean hasMemory = availableMemory > requiredMemory;
        
        logger.debug("💾 Memory check - Available: {} MB, Required: {} MB, Has enough: {}",
            availableMemory / (1024 * 1024L),
            requiredMemory / (1024 * 1024L),
            hasMemory);
        
        return hasMemory;
    }

    private int calculateMaxProcessableImages() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        
        // Dynamic calculation based on available memory
        if (maxMemory <= 128 * 1024 * 1024) { // 128MB or less
            return 3; // Very conservative for low-memory environments
        } else if (maxMemory <= 256 * 1024 * 1024) { // 256MB or less  
            return 5; // Moderate for medium-memory environments
        } else {
            return 8; // More generous for higher memory
        }
    }

    private void performMemoryCleanup() {
        try {
            Thread.sleep(800); // Shorter delay
            System.gc(); // Force garbage collection
            Thread.sleep(200); // Additional GC time
            
            // Log memory state after cleanup
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            long availableMemory = maxMemory - usedMemory;
            
            logger.debug("🧹 Memory after cleanup - Used: {} MB, Available: {} MB",
                usedMemory / (1024 * 1024),
                availableMemory / (1024 * 1024));
                
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
 // Upload original file without processing (same as before)
    private String uploadOriginalImage(MultipartFile file) throws IOException {
        validationService.validateFile(file);
        
        logger.info("📤 Uploading original image: {} ({} MB)", 
            file.getOriginalFilename(), 
            String.format("%.1f", file.getSize() / (1024.0 * 1024.0)));
        
        String uniqueFilename = generateUniqueFilename(file, null);
        String presignedUrl = s3Service.generatePresignedUrl(uniqueFilename);
        
        s3Service.uploadFile(presignedUrl, file.getBytes(), file.getContentType());
        
        logger.info("✅ Original image uploaded: {} MB", 
            String.format("%.1f", file.getSize() / (1024.0 * 1024.0)));
        
        return extractPublicUrl(presignedUrl);
    }

    // Single file upload with processing
    public String uploadAndProcessImage(MultipartFile file) throws IOException {
        validationService.validateFile(file);
        
        logger.info("🔄 Processing image: {} ({} MB)", 
            file.getOriginalFilename(), 
            String.format("%.1f", file.getSize() / (1024.0 * 1024.0)));
        
        // Process image (resize + compress) - this happens one at a time
        byte[] processedImage = imageProcessingService.processImage(file);
        
        String uniqueFilename = generateUniqueFilename(file, null);
        String presignedUrl = s3Service.generatePresignedUrl(uniqueFilename);
        
        // Upload processed image
        s3Service.uploadFile(presignedUrl, processedImage, "image/jpeg");
        
        logger.info("✅ Image processed: {} MB -> {} KB ({}% reduction)",
            String.format("%.1f", file.getSize() / (1024.0 * 1024.0)),
            String.format("%.0f", processedImage.length / 1024.0),
            String.format("%.0f", (1 - (double) processedImage.length / file.getSize()) * 100));
        
        return extractPublicUrl(presignedUrl);
    }

    // Single file upload with custom filename support
    public String uploadFile(MultipartFile file, String customFilename) throws IOException {
        validationService.validateFile(file);
        return uploadImageWithRetry(file, customFilename);
    }

    // Presigned URL generation
    public String generatePresignedUrl(String fileName) {
        return s3Service.generatePresignedUrl(fileName);
    }

    // Private helper methods
    private String uploadImageWithRetry(MultipartFile file, String customFilename) throws IOException {
        final int maxAttempts = 3;
        IOException lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                String uniqueFilename = generateUniqueFilename(file, customFilename);
                return uploadSingleImage(file, uniqueFilename);
            } catch (IOException e) {
                lastException = e;
                logger.warn("Upload attempt {}/{} failed for {}: {}", 
                    attempt, maxAttempts, file.getOriginalFilename(), e.getMessage());
                
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(1000 * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new ImageUploadException("Upload interrupted", ie);
                    }
                }
            }
        }
        throw new ImageUploadException(
            String.format("Failed to upload %s after %d attempts", 
                file.getOriginalFilename(), maxAttempts), 
            lastException);
    }

    private String uploadSingleImage(MultipartFile file, String uniqueFilename) throws IOException {
        return uploadAndProcessImage(file);
    }

    private String generateUniqueFilename(MultipartFile file, String customName) {
        String originalName = customName != null ? customName : file.getOriginalFilename();
        String extension = "jpg"; // Always use jpg after processing
        String baseName = UUID.randomUUID().toString();
        return String.format("%s/%s.%s", s3Folder, baseName, extension.toLowerCase());
    }

    private String extractPublicUrl(String presignedUrl) {
        return presignedUrl.split("\\?")[0];
    }

 // In RealEstateImageService.java - enhance deleteImages method
    public void deleteImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        List<String> failedDeletions = new ArrayList<>();
        List<String> s3Keys = new ArrayList<>();
        
        // Extract S3 keys first
        for (String url : imageUrls) {
            try {
                String key = extractS3Key(url);
                s3Keys.add(key);
            } catch (Exception e) {
                logger.warn("⚠️ Could not extract S3 key from URL: {}", url);
                failedDeletions.add(url);
            }
        }
        
        // Use batch delete if available
        if (s3Service instanceof RealS3Service && !s3Keys.isEmpty()) {
            try {
                ((RealS3Service) s3Service).deleteFiles(s3Keys);
                logger.info("✅ Batch deleted {} images from S3", s3Keys.size());
            } catch (Exception e) {
                logger.warn("⚠️ Batch delete failed, falling back to individual deletes: {}", e.getMessage());
                // Fall back to individual deletes
                s3Keys.forEach(key -> {
                    try {
                        s3Service.deleteFile(key);
                    } catch (Exception ex) {
                        logger.error("❌ Failed to delete image from S3: {}", key, ex);
                        failedDeletions.add(key);
                    }
                });
            }
        } else {
            // Individual deletes for non-RealS3Service implementations
            s3Keys.forEach(key -> {
                try {
                    s3Service.deleteFile(key);
                } catch (Exception e) {
                    logger.error("❌ Failed to delete image from S3: {}", key, e);
                    failedDeletions.add(key);
                }
            });
        }
        
        if (!failedDeletions.isEmpty()) {
            logger.warn("⚠️ Failed to delete {} images from S3", failedDeletions.size());
            // Consider sending to a dead letter queue or monitoring service
        }
    }

    private String extractS3Key(String imageUrl) {
        try {
            java.net.URI uri = new java.net.URI(imageUrl);
            String path = uri.getPath();
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (java.net.URISyntaxException e) {
            logger.warn("Invalid image URL format: {}", imageUrl);
            // Fallback: extract key from URL
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("/([^/]+/[^/]+)$");
            java.util.regex.Matcher matcher = pattern.matcher(imageUrl);
            return matcher.find() ? matcher.group(1) : 
                imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
        }
    }
}