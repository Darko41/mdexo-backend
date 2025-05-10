package com.doublez.backend.service.s3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;


@Service
@Profile("dev")
public class MockS3Service implements S3Service {
	
	private static final Logger logger = LoggerFactory.getLogger(MockS3Service.class);
	private final Path uploadDir;
	
	public MockS3Service(@Value("${app.mock.upload-dir:/uploads}") String mockUploadDir) {
		this.uploadDir = Paths.get(mockUploadDir).toAbsolutePath().normalize();
		ensureUploadDirExists();
		logger.info("Mock S3 Active. Uploads to: {}", uploadDir);
	}
	
	@Override
	public String generatePresignedUrl(String fileName) {
		String cleanName = cleanFileName(fileName);
		return "mock://s3/" + uploadDir.getFileName() + "/" + cleanName;
	}


	@Override
	public void uploadFile(String presignedUrl, byte[] data, String contentType) throws IOException {
		Path filePath = uploadDir.resolve(extractFileName(presignedUrl));
		Files.createDirectories(filePath.getParent());
		Files.write(filePath, data);
		
		logger.info("Mock Upload: {} ({} bytes, {})",
				filePath.getFileName(), data.length, contentType);
	}

	private String extractFileName(String url) {
		String path = URI.create(url).getPath();
		return path.substring(path.lastIndexOf('/') + 1);
	}
	
	private void ensureUploadDirExists() {
		try {
			Files.createDirectories(uploadDir);
		} catch (IOException  e) {
			throw new RuntimeException("Cannot create upload directory: " + uploadDir, e);
		}
	}

	private String cleanFileName(String fileName) {
	    return fileName.replaceAll("[^a-zA-Z0-9/.-]", "_");
	}

	@Override
	public void uploadFileStreaming(String presignedUrl, InputStream data, long contentLength, String contentType)
			throws IOException {
		Path filePath = uploadDir.resolve(extractFileName(presignedUrl));
	    Files.createDirectories(filePath.getParent());
	    
	    try (OutputStream out = Files.newOutputStream(filePath)) {
	        data.transferTo(out); // Java 9+ efficient stream copy
	    }
	    
	    logger.info("Mock Streaming Upload: {} ({} bytes, {})", 
	        filePath.getFileName(), contentLength, contentType);
		
	}

	@Override
	public void deleteFile(String key) {
	    // Sanitize each part of the key
	    Path filePath = uploadDir;
	    for (String part : key.split("/")) {
	        String cleanedPart = part.replaceAll("[^a-zA-Z0-9.-]", "_");
	        filePath = filePath.resolve(cleanedPart);
	    }
	    filePath = filePath.normalize();

	    // Security check
	    if (!filePath.startsWith(uploadDir)) {
	        throw new SecurityException("Invalid key: " + key);
	    }

	    try {
	        if (Files.exists(filePath)) {
	            Files.delete(filePath);
	            logger.info("Mock Deleted: {}", key);
	        } else {
	            logger.warn("Mock file not found: {}", key);
	        }
	    } catch (IOException e) {
	        throw new RuntimeException("Failed to delete mock file: " + key, e);
	    }
	}
}
