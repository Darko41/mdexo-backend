package com.doublez.backend.exception;

import java.util.List;

public class InvalidFileTypeException extends InvalidFileException{
	
	private final List<String> allowedTypes;

    public InvalidFileTypeException(String message, List<String> allowedTypes) {
        super(message);
        this.allowedTypes = allowedTypes;
    }

    public List<String> getAllowedTypes() {
        return allowedTypes;
    }

}
