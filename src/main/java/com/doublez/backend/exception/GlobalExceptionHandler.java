package com.doublez.backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.doublez.backend.response.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
		return ResponseEntity.internalServerError()
				.body(ApiResponse.error("Operation failed: " + e.getMessage()));
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<?>> handleGenericException(Exception e) {
		return ResponseEntity.internalServerError()
				.body(ApiResponse.error("An unexpected error occurred: " + e.getMessage()));
	}
	
}
