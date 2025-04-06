package com.doublez.backend.exception;

import org.springframework.http.HttpStatus;

public class RealEstateGetAllException extends ApiException {

	public RealEstateGetAllException(String message) {
		super(message, HttpStatus.BAD_REQUEST);
		
	}
	
}
