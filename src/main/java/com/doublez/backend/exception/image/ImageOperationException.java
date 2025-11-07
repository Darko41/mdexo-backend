package com.doublez.backend.exception.image;

public class ImageOperationException extends RuntimeException {
    public ImageOperationException(String message) {
        super(message);
    }
    
    public ImageOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}