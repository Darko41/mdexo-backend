package com.doublez.backend.service.s3;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface S3Service {
	String generatePresignedUrl(String fileName);
    void uploadFile(String presignedUrl, byte data[], String contentType) throws IOException;
    void uploadFileStreaming(String presignedUrl, InputStream data, long contentLength, String contentType) throws IOException;
    void deleteFile(String key);
    List<String> listObjects(String prefix);
}
