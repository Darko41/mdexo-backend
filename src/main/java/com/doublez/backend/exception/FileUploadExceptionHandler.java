package com.doublez.backend.exception;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;

@ControllerAdvice
public class FileUploadExceptionHandler {

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Map<String, Object>> handleMultipartException(MultipartException ex) {
        String message = ex.getMessage().contains("size") 
            ? "File exceeds maximum size limit (50MB)"
            : "Invalid file upload request";
        
        return ResponseEntity.badRequest().body(Map.of(
            "status", "error",
            "message", message,
            "data", null
        ));
    }
}
