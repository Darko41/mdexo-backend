package com.doublez.backend.service.validation;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.doublez.backend.exception.FileSizeException;
import com.doublez.backend.exception.InvalidFileException;
import com.doublez.backend.exception.InvalidFileTypeException;

@Service
public class FileValidationService {
	private static final Set<String> EXECUTABLE_EXTENSIONS = Set.of(
	        "exe", "bat", "cmd", "sh", "dll", "msi", "jar", "php", "js"
	    );
	
    private static final Logger logger = LoggerFactory.getLogger(FileValidationService.class);
    
    private final Set<String> allowedExtensions;
    private final List<String> allowedMimeTypes;
    private final long maxFileSize; // Hardcoded 20MB limit

    public FileValidationService(
            @Value("#{'${app.upload.allowed-extensions}'.split(',')}") List<String> allowedExtensions,
            @Value("#{'${app.upload.allowed-mime-types}'.split(',')}") List<String> allowedMimeTypes,
            @Value("${app.upload.max-size}") long maxFileSize) { // Add this parameter
        
        this.allowedExtensions = allowedExtensions.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        this.allowedMimeTypes = allowedMimeTypes;
        this.maxFileSize = maxFileSize; // Use the injected value
        
        logger.info("File validation configured - Extensions: {}, MIME types: {}, Max size: {} bytes", 
            allowedExtensions, allowedMimeTypes, maxFileSize);
    }

    
    public void validateFile(MultipartFile file) {
        validateNotNull(file);
        validateFilename(file.getOriginalFilename());
        validateNotExecutable(file);
        validateSize(file);
        validateType(file);
        validateExtension(file);
    }
    
    private void validateNotExecutable(MultipartFile file) {
        String filename = file.getOriginalFilename().toLowerCase();
        
        // Check both extension and magic numbers
        if (isExecutableExtension(filename) || isExecutableContent(file)) {
            throw new SecurityException("Executable files are not allowed");
        }
    }

    private boolean isExecutableExtension(String filename) {
        String ext = FilenameUtils.getExtension(filename);
        return EXECUTABLE_EXTENSIONS.contains(ext);
    }
    
    private boolean isExecutableContent(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            is.read(header);
            
            // Check for EXE/DLL magic numbers
            return (header[0] == 'M' && header[1] == 'Z') ||  // DOS/Windows
                   (header[0] == 0x7F && header[1] == 'E' &&  // ELF Unix
                    header[2] == 'L' && header[3] == 'F');
        } catch (IOException e) {
            throw new InvalidFileException("Could not verify file content");
        }
    }
    
    private void validateNotNull(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File cannot be empty");
        }
    }

    private void validateSize(MultipartFile file) {
        if (file.getSize() > maxFileSize) {
            throw new FileSizeException(
                String.format("File '%s' exceeds %dMB limit (%.2f MB)", 
                    file.getOriginalFilename(),
                    maxFileSize / (1024 * 1024), // Show limit in MB
                    file.getSize() / (1024.0 * 1024.0)),
                maxFileSize
            );
        }
    }

    private void validateType(MultipartFile file) {
        String contentType = file.getContentType();
        if (!allowedMimeTypes.contains(contentType)) {
            throw new InvalidFileTypeException(
                String.format("Invalid MIME type '%s' for file '%s'", 
                    contentType, file.getOriginalFilename()),
                allowedMimeTypes
            );
        }
    }

    private void validateExtension(MultipartFile file) {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            throw new InvalidFileTypeException(
                String.format("Invalid extension '%s' for file '%s'", 
                    extension, file.getOriginalFilename()),
                List.copyOf(allowedExtensions)
            );
        }
    }

    private void validateFilename(String filename) {
        if (filename == null) {
            throw new InvalidFileException("Filename cannot be null");
        }
        
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new SecurityException("Potentially malicious filename detected: " + filename);
        }
    }

    // Bulk validation for multiple files
    public void validateFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new InvalidFileException("No files provided");
        }
        
        for (MultipartFile file : files) {
            validateFile(file);
        }
    }
}
