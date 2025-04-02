package com.doublez.backend.customexception;

public class InvalidImageException extends RuntimeException{
	public InvalidImageException(String message) {
        super(message);
    }
}
