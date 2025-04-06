package com.doublez.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.doublez.backend.exception.InvalidImageException;

@Service
public class FileValidationService {
    
    public void validateFiles(MultipartFile[] files, List<String> allowedMimeTypes, long maxSize) {
        if (files == null) return;
        
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new InvalidImageException("File cannot be empty");
            }
            
            if (!allowedMimeTypes.contains(file.getContentType())) {
                throw new InvalidImageException(
                    "Invalid file type. Allowed: " + allowedMimeTypes);
            }
            
            if (file.getSize() > maxSize) {
                throw new InvalidImageException(
                    "File too large. Max size: " + maxSize + " bytes");
            }
            
            validateFilename(file.getOriginalFilename());
        }
    }
    
    private void validateFilename(String filename) {
        if (filename == null || filename.contains("..") || filename.contains("/")) {
            throw new InvalidImageException("Invalid filename");
        }
    }
}
