package com.doublez.backend.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
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
	
	@ExceptionHandler(SelfDeletionException.class)
	public ResponseEntity<ApiResponse<Map<String, Object>>> handleSelfDeletion(
	        SelfDeletionException e) {
	    return ResponseEntity.status(HttpStatus.FORBIDDEN)
	            .body(ApiResponse.error(
	                e.getMessage(),
	                e.getDetails() // Now this will work
	            ));
	}
	
	@ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Void>> handleNullPointer(NullPointerException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Invalid data: missing required relationship"));
    }
	
	@ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex) {
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }
	
}
