package com.doublez.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.service.S3Service;

@RestController
@RequestMapping("api/s3")
public class ImageUploadS3Controller {
	
	private final S3Service s3Service;
	
	public ImageUploadS3Controller(S3Service s3Service) {
		this.s3Service = s3Service;
	}
	
	@GetMapping("/generate-presigned-url")
	public String generatePresignedUrl(@RequestParam String fileName) {
		return s3Service.generatePresignedUrl(fileName);
	}

}
