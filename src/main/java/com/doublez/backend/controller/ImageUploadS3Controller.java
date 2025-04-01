package com.doublez.backend.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.doublez.backend.service.S3Service;

@RestController
@RequestMapping("api/s3")
public class ImageUploadS3Controller {
	
	private final S3Service s3Service;
	private static final Logger logger = LoggerFactory.getLogger(ImageUploadS3Controller.class);
	
	public ImageUploadS3Controller(S3Service s3Service) {
		this.s3Service = s3Service;
	}
	
	@GetMapping("/generate-presigned-url")
	public ResponseEntity<Map<String, String>> generatePresignedUrl(
			@RequestParam String fileName,
			@RequestParam(required = false) String contentType) {
		
		try {
			
			String url = s3Service.generatePresignedUrl(fileName);
			
			Map<String, String> response = new HashMap<>();
			response.put("url", url);
			response.put("type", "mock".equals(url.substring(0, 4)) ? "MOCK" : "S3");
			
			return ResponseEntity.ok(response);
					
		} catch (Exception e) {
			logger.error("URL generation failed", e);
			return ResponseEntity.internalServerError().build();
		}
	}
	
	@PostMapping("/upload")
	public ResponseEntity<String> uploadFile(
			@RequestParam MultipartFile file,
			@RequestParam(required = false) String fileName) throws IOException {
		
		String targetFileName = (fileName != null) ? fileName : file.getOriginalFilename();
		
		String url = s3Service.generatePresignedUrl(targetFileName);
		
		if (file.getSize() > 10_000_000) {
			s3Service.uploadFileStreaming(
					url,
					file.getInputStream(),
					file.getSize(),
					file.getContentType());
			return ResponseEntity.ok("Large file upload started to: " + url);
		}
		else {
			s3Service.uploadFile(
		            url,
		            file.getBytes(),
		            file.getContentType()
		    );
			
			return ResponseEntity.ok("Small file uploaded to: " + url);
		}
		
	}


}
