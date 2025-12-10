package com.doublez.backend.exception;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.doublez.backend.exception.image.ImageOperationException;
import com.doublez.backend.exception.image.ImageUploadException;
import com.doublez.backend.exception.image.ImageValidationException;
import com.doublez.backend.response.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Unified handler for all custom exceptions
    @ExceptionHandler({
        ApiException.class,
        SelfDeletionException.class,
        ResourceNotFoundException.class,
        FileSizeException.class,
        InvalidFileTypeException.class,
        NullPointerException.class,
        ImageOperationException.class,        
        ImageValidationException.class,       
        ImageUploadException.class,
        LimitationExceededException.class,
        UserNotFoundException.class,
        FeatureNotImplementedException.class,  // 🆕 ADDED
        BusinessRuleException.class           // 🆕 ADDED (if you have this)
    })
    public ResponseEntity<ApiResponse<?>> handleCustomExceptions(Exception ex) {
        HttpStatus status = determineStatus(ex);
        logger.error("Handling {}: {}", ex.getClass().getSimpleName(), ex.getMessage());

        if (ex instanceof FileSizeException sizeEx) {
            return ResponseEntity.status(status)
                .body(ApiResponse.error(
                    ex.getMessage(),
                    Map.of("maxAllowedSize", sizeEx.getMaxAllowedSize())
                ));
        }
        
        if (ex instanceof InvalidFileTypeException typeEx) {
            return ResponseEntity.status(status)
                .body(ApiResponse.error(
                    ex.getMessage(),
                    Map.of("allowedTypes", typeEx.getAllowedTypes())
                ));
        }

        if (ex instanceof SelfDeletionException sdEx) {
            return ResponseEntity.status(status)
                .body(ApiResponse.error(
                    ex.getMessage(),
                    sdEx.getDetails()
                ));
        }

        // 🆕 ADD HANDLING FOR FEATURE NOT IMPLEMENTED
        if (ex instanceof FeatureNotImplementedException) {
            return ResponseEntity.status(status)
                .body(ApiResponse.error(
                    ex.getMessage(),
                    Map.of("errorType", "FEATURE_NOT_IMPLEMENTED", "status", "coming_soon")
                ));
        }

        if (ex instanceof LimitationExceededException) {
            return ResponseEntity.status(status)
                .body(ApiResponse.error(
                    ex.getMessage(),
                    Map.of("errorType", "LIMITATION_EXCEEDED")
                ));
        }

        if (ex instanceof UserNotFoundException) {
            return ResponseEntity.status(status)
                .body(ApiResponse.error(
                    ex.getMessage(),
                    Map.of("errorType", "USER_NOT_FOUND")
                ));
        }

        // 🆕 ADD HANDLING FOR BUSINESS RULE EXCEPTIONS
        if (ex instanceof BusinessRuleException) {
            return ResponseEntity.status(status)
                .body(ApiResponse.error(
                    ex.getMessage(),
                    Map.of("errorType", "BUSINESS_RULE_VIOLATION")
                ));
        }

        if (ex instanceof ImageOperationException) {
            return ResponseEntity.status(status)
                .body(ApiResponse.error("Image operation failed: " + ex.getMessage()));
        }
        
        if (ex instanceof ImageValidationException) {
            return ResponseEntity.status(status)
                .body(ApiResponse.error(ex.getMessage()));
        }
        
        if (ex instanceof ImageUploadException) {
            return ResponseEntity.status(status)
                .body(ApiResponse.error("Image upload failed: " + ex.getMessage()));
        }

        return ResponseEntity.status(status)
            .body(ApiResponse.error(ex.getMessage()));
    }

    // Fallback for unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnexpectedException(Exception ex) {
        logger.error("Unhandled exception", ex);
        return ResponseEntity.internalServerError()
            .body(ApiResponse.error("An unexpected error occurred"));
    }

    private HttpStatus determineStatus(Exception ex) {
        if (ex instanceof SelfDeletionException) return HttpStatus.FORBIDDEN;
        if (ex instanceof ResourceNotFoundException) return HttpStatus.NOT_FOUND;
        if (ex instanceof FileSizeException) return HttpStatus.BAD_REQUEST;
        if (ex instanceof InvalidFileTypeException) return HttpStatus.BAD_REQUEST;
        if (ex instanceof NullPointerException) return HttpStatus.BAD_REQUEST;
        if (ex instanceof ImageValidationException) return HttpStatus.BAD_REQUEST;
        if (ex instanceof ImageUploadException) return HttpStatus.INTERNAL_SERVER_ERROR; 
        if (ex instanceof ImageOperationException) return HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex instanceof LimitationExceededException) return HttpStatus.FORBIDDEN;
        if (ex instanceof UserNotFoundException) return HttpStatus.NOT_FOUND;
        if (ex instanceof FeatureNotImplementedException) return HttpStatus.NOT_IMPLEMENTED; // 🆕 501 status
        if (ex instanceof BusinessRuleException) return HttpStatus.BAD_REQUEST;             // 🆕 400 status
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}