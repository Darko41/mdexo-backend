package com.doublez.backend.service;

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
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@Profile("!dev")
public class RealS3Service implements S3Service {
	private static final Logger logger = LoggerFactory.getLogger(RealS3Service.class);
	private final S3Presigner presigner;
	private final String bucketName;
	
	public RealS3Service(S3Presigner presigner,
					@Value("${aws.s3.bucket}") String bucketName) {
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
	}

	@Override
	public void uploadFileStreaming(String presignedUrl, InputStream data, long contentLength, String contentType)
			throws IOException {
		HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(presignedUrl))
				.PUT(HttpRequest.BodyPublishers.ofInputStream(() -> data))
				.header("Content-Type", contentType)
				.header("Content-Length", String.valueOf(contentLength))
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

}