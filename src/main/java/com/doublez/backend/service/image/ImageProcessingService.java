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
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageProcessingService {
	
private static final Logger logger = LoggerFactory.getLogger(ImageProcessingService.class);
    
    // Target dimensions for Full HD
    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1080;
    private static final float QUALITY = 0.8f; // 80% quality
    
    public byte[] processImage(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String formatName = getOutputFormat(originalFilename);
        
        try (InputStream input = file.getInputStream()) {
            BufferedImage originalImage = ImageIO.read(input);
            
            if (originalImage == null) {
                throw new IOException("Unsupported image format: " + originalFilename);
            }
            
            // Calculate new dimensions maintaining aspect ratio
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            Dimension newSize = calculateScaledDimensions(originalWidth, originalHeight);
            
            // Create scaled image
            BufferedImage scaledImage = scaleImage(originalImage, newSize.width, newSize.height);
            
            // Convert to output format
            return convertToBytes(scaledImage, formatName);
            
        } catch (Exception e) {
            logger.error("Failed to process image: {}", originalFilename, e);
            throw new IOException("Image processing failed: " + e.getMessage(), e);
        }
    }
    
    private Dimension calculateScaledDimensions(int originalWidth, int originalHeight) {
        double widthRatio = (double) MAX_WIDTH / originalWidth;
        double heightRatio = (double) MAX_HEIGHT / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);
        
        // Don't upscale small images
        ratio = Math.min(ratio, 1.0);
        
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);
        
        logger.debug("Resizing from {}x{} to {}x{}", 
            originalWidth, originalHeight, newWidth, newHeight);
            
        return new Dimension(newWidth, newHeight);
    }
    
    private BufferedImage scaleImage(BufferedImage originalImage, int width, int height) {
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        
        // Better quality scaling
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                            RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(originalImage, 0, 0, width, height, null);
        g2d.dispose();
        
        return scaledImage;
    }
    
    private byte[] convertToBytes(BufferedImage image, String formatName) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            if ("webp".equalsIgnoreCase(formatName)) {
                // WebP conversion (requires webp-imageio)
                ImageIO.write(image, "WEBP", baos);
            } else {
                // JPEG with quality setting
                Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
                if (writers.hasNext()) {
                    ImageWriter writer = writers.next();
                    try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                        writer.setOutput(ios);
                        
                        ImageWriteParam param = writer.getDefaultWriteParam();
                        if (param.canWriteCompressed()) {
                            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                            param.setCompressionQuality(QUALITY);
                        }
                        
                        writer.write(null, new IIOImage(image, null, null), param);
                    }
                    writer.dispose();
                } else {
                    // Fallback to standard JPEG
                    ImageIO.write(image, "JPEG", baos);
                }
            }
            
            byte[] result = baos.toByteArray();
            logger.info("Processed image: {} -> {} bytes ({} KB)", 
                formatName, result.length, result.length / 1024);
                
            return result;
        }
    }
    
    private String getOutputFormat(String filename) {
        if (filename != null) {
            String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
            // Prefer WebP for better compression, fallback to JPEG
            return "webp".equals(ext) ? "webp" : "jpeg";
        }
        return "jpeg";
    }
    
    public long getEstimatedSize(int width, int height) {
        // Rough estimation: width * height * 3 bytes * compression ratio
        return (long) (width * height * 3 * 0.1); // ~10% of original size
    }

}
