package com.doublez.backend.exception;

public class LimitationExceededException extends RuntimeException {
 
 public LimitationExceededException() {
     super();
 }
 
 public LimitationExceededException(String message) {
     super(message);
 }
 
 public LimitationExceededException(String message, Throwable cause) {
     super(message, cause);
 }
 
 public LimitationExceededException(Throwable cause) {
     super(cause);
 }
 
 // Optional: Constructor with limitation details
 public LimitationExceededException(String resourceType, Long currentCount, Long maxAllowed) {
     super(String.format("%s limit exceeded. Current: %d, Maximum allowed: %d", 
           resourceType, currentCount, maxAllowed));
 }
 
 public LimitationExceededException(String resourceType, Long currentCount, Long maxAllowed, String tierName) {
     super(String.format("%s limit exceeded for %s tier. Current: %d, Maximum allowed: %d", 
           resourceType, tierName, currentCount, maxAllowed));
 }
}