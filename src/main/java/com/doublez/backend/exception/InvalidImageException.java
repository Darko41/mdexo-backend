package com.doublez.backend.exception;

public class InvalidImageException extends RuntimeException{
	public InvalidImageException(String message) {
        super(message);
    }
}
