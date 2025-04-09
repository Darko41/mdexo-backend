package com.doublez.backend.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
        NullPointerException.class
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
                    sdEx.getDetails() // Using your existing details field
                ));
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
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
