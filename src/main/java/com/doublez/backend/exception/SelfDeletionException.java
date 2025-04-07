package com.doublez.backend.exception;

import java.util.Map;

public class SelfDeletionException extends RuntimeException {
	
	 private final Map<String, Object> details;
	    
	    public SelfDeletionException(String message, Map<String, Object> details) {
	        super(message);
	        this.details = details;
	    }
	    
	    public Map<String, Object> getDetails() {
	        return details;
	    }

}
