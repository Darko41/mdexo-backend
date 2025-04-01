package com.doublez.backend.service;

import java.io.IOException;
import java.io.InputStream;

public interface S3Service {
	String generatePresignedUrl(String fileName);
    void uploadFile(String presignedUrl, byte data[], String contentType) throws IOException;
    void uploadFileStreaming(String presignedUrl, InputStream data, long contentLength, String contentType) throws IOException;
}
