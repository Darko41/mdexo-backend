package com.doublez.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RealEstateNotFoundException extends ApiException {

	public RealEstateNotFoundException(Long id) {
		super("Real estate not found with id: " + id, HttpStatus.NOT_FOUND);
	}
	
}
