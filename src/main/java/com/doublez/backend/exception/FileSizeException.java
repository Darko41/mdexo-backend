package com.doublez.backend.exception;

public class FileSizeException extends RuntimeException{
	private final long maxAllowedSize;

    public FileSizeException(String message, long maxAllowedSize) {
        super(message);
        this.maxAllowedSize = maxAllowedSize;
    }

    public long getMaxAllowedSize() {
        return maxAllowedSize;
    }

}
