package com.doublez.backend.exception;

public class UnauthorizedAccessException extends RuntimeException  {
	public UnauthorizedAccessException(String message) {
		super(message);
	}
}
