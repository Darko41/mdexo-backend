package com.doublez.backend.service.image;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(ImageProcessingService.class);
    
    // Target dimensions for resizing
    private static final int TARGET_WIDTH = 1920;
    private static final int TARGET_HEIGHT = 1080;
    private static final float QUALITY = 0.8f;
    
    public byte[] processImage(MultipartFile file) {
        // Validate input
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        
        ByteArrayOutputStream outputStream = null;
        BufferedImage image = null;
        
        try {
            logger.info("🔄 Processing image: {} ({} MB)", 
                file.getOriginalFilename(), 
                String.format("%.1f", file.getSize() / (1024.0 * 1024.0)));
            
            // Read image with memory monitoring
            image = readImageSafely(file);
            
            // Calculate new dimensions maintaining aspect ratio
            int newWidth = TARGET_WIDTH;
            int newHeight = TARGET_HEIGHT;
            int originalWidth = image.getWidth();
            int originalHeight = image.getHeight();
            
            // Maintain aspect ratio
            float aspectRatio = (float) originalWidth / originalHeight;
            if (originalWidth > originalHeight) {
                newHeight = Math.round(TARGET_WIDTH / aspectRatio);
            } else {
                newWidth = Math.round(TARGET_HEIGHT * aspectRatio);
            }
            
            // Only resize if image is larger than target
            if (originalWidth <= TARGET_WIDTH && originalHeight <= TARGET_HEIGHT) {
                logger.info("📏 Image within target dimensions, skipping resize");
                newWidth = originalWidth;
                newHeight = originalHeight;
            }
            
            // Create scaled instance (more memory efficient)
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedImage.createGraphics();
            
            // Configure quality settings
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw resized image
            g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
            g2d.dispose();
            
            // Force garbage collection of original image
            image = null;
            System.gc();
            
            // Convert to WebP or JPEG
            outputStream = new ByteArrayOutputStream();
            
            // Try WebP first, fallback to JPEG
            String formatName = getOutputFormat(file.getContentType());
            
            ImageWriter writer = ImageIO.getImageWritersByFormatName(formatName).next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            
            // Set compression for JPEG
            if (writeParam.canWriteCompressed()) {
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionQuality(QUALITY);
            }
            
            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
            writer.setOutput(imageOutputStream);
            writer.write(null, new IIOImage(resizedImage, null, null), writeParam);
            
            // Clean up
            writer.dispose();
            imageOutputStream.close();
            
            byte[] processedImage = outputStream.toByteArray();
            
            logger.info("✅ Image processed: {} MB -> {} KB ({}% reduction)",
                String.format("%.1f", file.getSize() / (1024.0 * 1024.0)),
                String.format("%.0f", processedImage.length / 1024.0),
                String.format("%.0f", (1 - (double) processedImage.length / file.getSize()) * 100));
            
            return processedImage;
            
        } catch (Exception e) {
            logger.error("❌ Error processing image: {}", e.getMessage());
            // Fallback: return original file bytes
            try {
                return file.getBytes();
            } catch (IOException ioException) {
                throw new RuntimeException("Failed to process image and fallback failed", ioException);
            }
        } finally {
            // Ensure resources are cleaned up
            if (outputStream != null) {
                try { outputStream.close(); } catch (IOException e) { /* ignore */ }
            }
            if (image != null) {
                image.flush();
            }
        }
    }
    
    private BufferedImage readImageSafely(MultipartFile file) throws IOException {
        // Use ImageIO with memory monitoring
        InputStream inputStream = file.getInputStream();
        
        // Read image efficiently
        ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
        Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
        
        if (!readers.hasNext()) {
            throw new IOException("No suitable image reader found for " + file.getContentType());
        }
        
        ImageReader reader = readers.next();
        reader.setInput(imageInputStream);
        
        BufferedImage image;
        try {
            // Read with minimal memory usage
            ImageReadParam param = reader.getDefaultReadParam();
            image = reader.read(0, param);
        } finally {
            reader.dispose();
            imageInputStream.close();
            inputStream.close();
        }
        
        return image;
    }
    
    private String getOutputFormat(String contentType) {
        if (contentType != null && contentType.equals("image/webp")) {
            return "webp";
        }
        // Default to JPEG for good compression
        return "jpeg";
    }
}