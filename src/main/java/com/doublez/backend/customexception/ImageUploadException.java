package com.doublez.backend.customexception;

public class ImageUploadException extends RuntimeException{
	public ImageUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
