package com.doublez.backend.service.s3;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
//@Profile("!dev")
public class RealS3Service implements S3Service {
	
	@Override
    public List<String> listObjects(String prefix) {
        List<String> objectKeys = new ArrayList<>();
        
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();
            
            ListObjectsV2Iterable responses = s3Client.listObjectsV2Paginator(request);
            
            for (ListObjectsV2Response response : responses) {
                response.contents().forEach(object -> {
                    objectKeys.add(object.key());
                });
            }
            
            logger.debug("📊 Listed {} objects with prefix '{}'", objectKeys.size(), prefix);
            
        } catch (Exception e) {
            logger.error("❌ Failed to list S3 objects with prefix '{}': {}", prefix, e.getMessage(), e);
            throw new RuntimeException("Failed to list S3 objects", e);
        }
        
        return objectKeys;
    }

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
	    HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

	    HttpRequest request = HttpRequest.newBuilder()
	            .uri(URI.create(presignedUrl))
	            .PUT(HttpRequest.BodyPublishers.ofInputStream(() -> data))
	            .header("Content-Type", contentType)
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
            logger.debug("🗑️ Attempting to delete S3 object: {}", key);
            
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            
            logger.info("✅ Successfully deleted file from S3: {}", key);
            
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                logger.warn("⚠️ File not found in S3 (may already be deleted): {}", key);
            } else if (e.statusCode() == 403) {
                logger.error("❌ Permission denied deleting S3 file: {}. Check IAM policies.", key);
                throw new RuntimeException("S3 delete permission denied - check IAM policies", e);
            } else {
                logger.error("❌ Failed to delete file from S3: {} - {}", key, e.awsErrorDetails().errorMessage());
                throw new RuntimeException("Failed to delete file from S3: " + key, e);
            }
        } catch (Exception e) {
            logger.error("❌ Unexpected error deleting S3 file: {}", key, e);
            throw new RuntimeException("Unexpected error deleting S3 file", e);
        }
    }
	
	/**
     * Batch delete multiple files (more efficient than individual deletes)
     */
    public void deleteFiles(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        
        // S3 supports up to 1000 objects per batch delete
        List<List<String>> batches = partitionList(keys, 1000);
        
        for (List<String> batch : batches) {
            try {
                List<ObjectIdentifier> objects = batch.stream()
                    .map(key -> ObjectIdentifier.builder().key(key).build())
                    .collect(Collectors.toList());
                
                DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder().objects(objects).build())
                    .build();
                
                s3Client.deleteObjects(deleteRequest);
                logger.info("✅ Batch deleted {} files from S3", batch.size());
                
            } catch (S3Exception e) {
                logger.error("❌ Batch delete failed for {} files: {}", batch.size(), e.awsErrorDetails().errorMessage());
                // Fall back to individual deletes
                batch.forEach(this::deleteFile);
            }
        }
    }
    
    /**
     * Helper method to split a list into batches
     */
    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            int end = Math.min(i + batchSize, list.size());
            batches.add(new ArrayList<>(list.subList(i, end)));
        }
        return batches;
    }

}