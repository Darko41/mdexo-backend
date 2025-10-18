package com.doublez.backend.service.s3;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@Profile("!dev")
public class RealS3Service implements S3Service {
	private static final Logger logger = LoggerFactory.getLogger(RealS3Service.class);
	private final S3Presigner presigner;
	private final String bucketName;
	private final S3Client s3Client;
	
	public RealS3Service(S3Client s3Client, 
            	S3Presigner presigner,
            	@Value("${aws.s3.bucket}") String bucketName) {
			this.s3Client = s3Client;
			this.presigner = presigner;
			this.bucketName = bucketName;
	}
	
	public String generatePresignedUrl(String fileName) {
		
		PutObjectPresignRequest request = PutObjectPresignRequest.builder()
				.signatureDuration(Duration.ofMinutes(15))
				.putObjectRequest(p -> p.bucket(bucketName).key(fileName))
				.build();
		
		String url = presigner.presignPutObject(request).url().toString();
		logger.debug("Generated presigner URL for {}", fileName);
		return url;
	}
	
	@Override
	public void uploadFile(String presignedUrl, byte[] data, String contentType) throws IOException {
		
		System.out.println("📤 Uploading to S3 - URL: " + presignedUrl);
        System.out.println("📤 Data length: " + data.length);
        System.out.println("📤 Content type: " + contentType);
		
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(presignedUrl))
				.PUT(HttpRequest.BodyPublishers.ofByteArray(data))
				.header("Content-Type", contentType)
				.timeout(Duration.ofSeconds(30))
				.build();
		
		try {
			HttpResponse<Void> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding());
			
			if (response.statusCode() != 200) {
				throw new IOException("Upload failed with status: " + response.statusCode());
			}
		} 
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Upload interrupted", e);
		}
		catch (HttpTimeoutException e) {
			throw new IOException("Upload timed out after 30 seconds", e);
		}
		
		System.out.println("✅ Upload completed successfully");
	}

	@Override
	public void uploadFileStreaming(String presignedUrl, InputStream data, long contentLength, String contentType)
	        throws IOException {
	    HttpClient client = HttpClient.newBuilder()
	            .connectTimeout(Duration.ofSeconds(10))
	            .build();

	    // Use BodyPublishers.ofByteArray for known content length, or let the client handle it
	    byte[] dataBytes;
	    try {
	        dataBytes = data.readAllBytes();
	    } catch (IOException e) {
	        throw new IOException("Failed to read input stream", e);
	    }

	    HttpRequest request = HttpRequest.newBuilder()
	            .uri(URI.create(presignedUrl))
	            .PUT(HttpRequest.BodyPublishers.ofByteArray(dataBytes))
	            .header("Content-Type", contentType)
	            // No need for Content-Length header when using ofByteArray
	            .timeout(Duration.ofSeconds(30))
	            .build();

	    try {
	        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
	        if (response.statusCode() != 200) {
	            throw new IOException("Upload failed with status: " + response.statusCode());
	        }
	    } catch (InterruptedException e) {
	        Thread.currentThread().interrupt();
	        throw new IOException("Upload interrupted", e);
	    }
	}

	@Override
	public void deleteFile(String key) {
	    try {
	        s3Client.deleteObject(DeleteObjectRequest.builder()
	                .bucket(bucketName)
	                .key(key)
	                .build());
	        logger.info("Deleted file from S3: {}", key);
	    } catch (S3Exception e) {
	        throw new RuntimeException("Failed to delete file: " + key, e);
	    }
	}

}