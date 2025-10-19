package com.doublez.backend.controller.image;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.doublez.backend.response.ImageUploadResponse;
import com.doublez.backend.service.realestate.RealEstateImageService;

@RestController
@RequestMapping("/api/images")
public class ImageUploadController {
	
private final RealEstateImageService imageService;
    
    public ImageUploadController(RealEstateImageService imageService) {
        this.imageService = imageService;
    }
    
    @PostMapping("/upload")
    public ResponseEntity<ImageUploadResponse> uploadImage(@RequestParam("image") MultipartFile file) {
        try {
            String imageUrl = imageService.uploadAndProcessImage(file);
            return ResponseEntity.ok(new ImageUploadResponse(imageUrl, "Image uploaded successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ImageUploadResponse(null, "Upload failed: " + e.getMessage()));
        }
    }


}
