package com.doublez.backend.exception;

public class FeatureNotImplementedException extends RuntimeException {
    
    public FeatureNotImplementedException() {
        super("This feature is not yet implemented. Please check back later.");
    }
    
    public FeatureNotImplementedException(String featureName) {
        super("Feature '" + featureName + "' is not yet implemented. Please check back later.");
    }
    
    public FeatureNotImplementedException(String featureName, String estimatedTimeline) {
        super("Feature '" + featureName + "' is not yet available. Estimated availability: " + estimatedTimeline);
    }
    
    public FeatureNotImplementedException(String message, Throwable cause) {
        super(message, cause);
    }
}