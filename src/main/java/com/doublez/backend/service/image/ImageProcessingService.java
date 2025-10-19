package com.doublez.backend.service.image;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    
    // Reduced target dimensions for production
    private static final int TARGET_WIDTH = 1280;  // Reduced from 1920
    private static final int TARGET_HEIGHT = 720;  // Reduced from 1080
    private static final float QUALITY = 0.6f;     // Reduced from 0.8f
    
    public byte[] processImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        
        // Quick memory check - more aggressive
        if (!hasEnoughMemory(25 * 1024 * 1024)) { // Only require 25MB
            logger.warn("⚠️ Low memory, using lightweight processing for: {}", file.getOriginalFilename());
            return lightweightProcess(file); // Use lightweight fallback
        }
        
        ByteArrayOutputStream outputStream = null;
        BufferedImage image = null;
        
        try {
            logger.info("🔄 Processing image: {} ({} MB)", 
                file.getOriginalFilename(), 
                String.format("%.1f", file.getSize() / (1024.0 * 1024.0)));
            
            // Use more memory-efficient reading
            image = readImageEfficiently(file);
            
            if (image == null) {
                return file.getBytes(); // Fallback to original
            }
            
            // Quick resize calculation
            int newWidth = image.getWidth();
            int newHeight = image.getHeight();
            
            // Only resize if significantly larger than target
            if (image.getWidth() > TARGET_WIDTH || image.getHeight() > TARGET_HEIGHT) {
                float scale = Math.min((float) TARGET_WIDTH / image.getWidth(), 
                                     (float) TARGET_HEIGHT / image.getHeight());
                newWidth = (int) (image.getWidth() * scale);
                newHeight = (int) (image.getHeight() * scale);
            }
            
            // Use faster, less memory-intensive scaling
            BufferedImage resizedImage = scaleImageFast(image, newWidth, newHeight);
            
            // Clean up original image immediately
            image.flush();
            image = null;
            System.gc();
            
            // Compress with lower quality
            outputStream = new ByteArrayOutputStream();
            compressImageFast(resizedImage, outputStream, QUALITY);
            
            byte[] processedImage = outputStream.toByteArray();
            
            logger.info("✅ Image processed: {} MB -> {} KB ({}% reduction)",
                String.format("%.1f", file.getSize() / (1024.0 * 1024.0)),
                String.format("%.0f", processedImage.length / 1024.0),
                String.format("%.0f", (1 - (double) processedImage.length / file.getSize()) * 100));
            
            return processedImage;
            
        } catch (Exception e) {
            logger.error("❌ Error processing image: {}", e.getMessage());
            return lightweightProcess(file); // Fallback to lightweight
        } finally {
            // Cleanup
            if (outputStream != null) {
                try { outputStream.close(); } catch (IOException e) { }
            }
            if (image != null) {
                image.flush();
            }
        }
    }
    
    // Lightweight processing - minimal memory usage
    private byte[] lightweightProcess(MultipartFile file) {
        try {
            // Just compress without resizing
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) return file.getBytes();
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // Fast compression only
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionQuality(0.5f); // Lower quality for speed
            
            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
            writer.setOutput(imageOutputStream);
            writer.write(null, new IIOImage(image, null, null), writeParam);
            
            writer.dispose();
            imageOutputStream.close();
            image.flush();
            
            byte[] result = outputStream.toByteArray();
            logger.info("⚡ Lightweight processed: {} MB -> {} KB ({}% reduction)",
                String.format("%.1f", file.getSize() / (1024.0 * 1024.0)),
                String.format("%.0f", result.length / 1024.0),
                String.format("%.0f", (1 - (double) result.length / file.getSize()) * 100));
            
            return result;
            
        } catch (Exception e) {
            logger.warn("⚠️ Lightweight processing failed, using original file");
            try {
                return file.getBytes();
            } catch (IOException ioException) {
                throw new RuntimeException("All processing failed", ioException);
            }
        }
    }
    
    private BufferedImage readImageEfficiently(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            // Use ImageIO with hints for memory efficiency
            ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
            
            if (!readers.hasNext()) return null;
            
            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInputStream);
                
                // Read with minimal settings
                ImageReadParam param = reader.getDefaultReadParam();
                return reader.read(0, param);
            } finally {
                reader.dispose();
                imageInputStream.close();
            }
        }
    }
    
    private BufferedImage scaleImageFast(BufferedImage source, int width, int height) {
        // Use faster scaling algorithm
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaled.createGraphics();
        
        // Faster, less quality rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                           RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                           RenderingHints.VALUE_RENDER_SPEED);
        
        g2d.drawImage(source, 0, 0, width, height, null);
        g2d.dispose();
        
        return scaled;
    }
    
    private void compressImageFast(BufferedImage image, OutputStream output, float quality) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        
        if (writeParam.canWriteCompressed()) {
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionQuality(quality);
        }
        
        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(output);
        writer.setOutput(imageOutputStream);
        writer.write(null, new IIOImage(image, null, null), writeParam);
        
        writer.dispose();
        imageOutputStream.close();
        image.flush();
    }
    
    private boolean hasEnoughMemory(long requiredMemory) {
        Runtime runtime = Runtime.getRuntime();
        long availableMemory = runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory());
        return availableMemory > requiredMemory;
    }
}