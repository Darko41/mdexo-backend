package com.doublez.backend.dto.realestate;

// RODO Look for usage of this class and remove if unneeded

import java.util.Collections;
import java.util.List;

public class RemoveImagesRequest {
    private List<String> imageUrls;

    // Default constructor
    public RemoveImagesRequest() {}

    // Constructor
    public RemoveImagesRequest(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    // Getters and setters
    public List<String> getImageUrls() {
        return imageUrls != null ? imageUrls : Collections.emptyList();
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
