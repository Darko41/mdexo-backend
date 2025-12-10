package com.doublez.backend.exception;

public class PermissionException extends RuntimeException {
    
    private String permission;
    private String resource;
    
    public PermissionException(String message) {
        super(message);
    }
    
    public PermissionException(String message, String permission, String resource) {
        super(message);
        this.permission = permission;
        this.resource = resource;
    }
    
    public PermissionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public String getPermission() {
        return permission;
    }
    
    public String getResource() {
        return resource;
    }
}
