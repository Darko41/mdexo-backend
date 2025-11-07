package com.doublez.backend.exception.image;

public class ImageValidationException extends RuntimeException {
    public ImageValidationException(String message) {
        super(message);
    }
}