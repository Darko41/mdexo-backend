package com.doublez.backend.service.s3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;


@Service
@Profile("dev")
public class MockS3Service implements S3Service {
    
    private static final Logger logger = LoggerFactory.getLogger(MockS3Service.class);
    private final Random random = new Random();
    
    // Different image categories for variety
    private final String[] imageCategories = {"house", "apartment", "building", "interior", "architecture"};
    
    public MockS3Service() {
        logger.info("Dev S3 Service Active - Using Lorem Picsum placeholder images");
    }
    
    @Override
    public String generatePresignedUrl(String fileName) {
        String cleanName = cleanFileName(fileName);
        String category = imageCategories[Math.abs(cleanName.hashCode()) % imageCategories.length];
        int uniqueId = Math.abs(cleanName.hashCode()) % 1000;
        
        // Return relevant-looking placeholder images
        return "https://loremflickr.com/800/600/" + category + ",realestate?random=" + uniqueId;
        
    }

    @Override
    public void uploadFile(String presignedUrl, byte[] data, String contentType) throws IOException {
        String fileName = extractFileName(presignedUrl);
        logger.info("DEV: Simulated upload of {} ({} bytes, {})", 
                fileName, data.length, contentType);
    }

    @Override
    public void uploadFileStreaming(String presignedUrl, InputStream data, long contentLength, String contentType) throws IOException {
        String fileName = extractFileName(presignedUrl);
        logger.info("DEV: Simulated stream upload of {} ({} bytes, {})", 
                fileName, contentLength, contentType);
        
        // Consume and discard the stream
        try {
            data.transferTo(OutputStream.nullOutputStream());
        } catch (IOException e) {
            // Ignore - we're just consuming the stream
        }
    }

    @Override
    public void deleteFile(String key) {
        logger.info("DEV: Simulated delete of {}", key);
    }

    private String extractFileName(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        } catch (Exception e) {
            return url.substring(url.lastIndexOf('/') + 1);
        }
    }

    private String cleanFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9/.-]", "_");
    }
}