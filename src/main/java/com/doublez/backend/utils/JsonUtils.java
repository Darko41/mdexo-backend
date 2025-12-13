package com.doublez.backend.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for JSON serialization/deserialization with proper type handling.
 * All JSON parsing logic should be centralized here.
 */
public class JsonUtils {
    
    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper objectMapper;
    
    static {
        objectMapper = new ObjectMapper();
        // Register Java 8 Time module for LocalDateTime, LocalDate, etc.
        objectMapper.registerModule(new JavaTimeModule());
        // Ignore null fields when serializing
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // Don't fail on unknown properties
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    // Prevent instantiation
    private JsonUtils() {}
    
    // ===== OBJECTMAPPER ACCESS =====
    
    /**
     * Get the shared ObjectMapper instance
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
    
    // ===== PARSE METHODS (FROM YOUR NotificationTemplate) =====
    
    /**
     * Parse JSON string to Map<K, V> with proper generic type handling
     * This replaces your parseJsonMap method
     */
    public static <K, V> Map<K, V> parseMap(String json, Class<K> keyClass, Class<V> valueClass) {
        if (isEmpty(json)) {
            return new HashMap<>();
        }
        try {
            JavaType type = objectMapper.getTypeFactory()
                    .constructMapType(Map.class, keyClass, valueClass);
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.warn("Failed to parse JSON to Map<{}, {}>: {}", 
                    keyClass.getSimpleName(), valueClass.getSimpleName(), e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * Parse JSON string to List<T> with proper generic type handling
     * This replaces your parseJsonList method
     */
    public static <T> List<T> parseList(String json, Class<T> elementClass) {
        if (isEmpty(json)) {
            return new ArrayList<>();
        }
        try {
            JavaType type = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, elementClass);
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.warn("Failed to parse JSON to List<{}>: {}", 
                    elementClass.getSimpleName(), e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // ===== COMMON TYPE HELPERS =====
    
    /**
     * Parse JSON string to Map<String, String> (most common case)
     */
    public static Map<String, String> parseStringMap(String json) {
        return parseMap(json, String.class, String.class);
    }
    
    /**
     * Parse JSON string to Map<String, Object> (flexible values)
     */
    public static Map<String, Object> parseStringObjectMap(String json) {
        return parseMap(json, String.class, Object.class);
    }
    
    /**
     * Parse JSON string to List<String> (common case)
     */
    public static List<String> parseStringList(String json) {
        return parseList(json, String.class);
    }
    
    /**
     * Parse JSON string to Map<String, Integer>
     */
    public static Map<String, Integer> parseStringIntegerMap(String json) {
        return parseMap(json, String.class, Integer.class);
    }
    
    /**
     * Parse JSON string to Map<String, Double>
     */
    public static Map<String, Double> parseStringDoubleMap(String json) {
        return parseMap(json, String.class, Double.class);
    }
    
    /**
     * Parse JSON string to Map<String, Boolean>
     */
    public static Map<String, Boolean> parseStringBooleanMap(String json) {
        return parseMap(json, String.class, Boolean.class);
    }
    
    // ===== TYPE REFERENCE METHODS (for complex nested generics) =====
    
    /**
     * Parse JSON string using TypeReference (for complex nested generics)
     */
    public static <T> T parseWithTypeReference(String json, TypeReference<T> typeReference) {
        if (isEmpty(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            log.warn("Failed to parse JSON with TypeReference: {}", e.getMessage());
            return null;
        }
    }
    
    // ===== SERIALIZATION METHODS =====
    
    /**
     * Convert object to JSON string
     */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert object to JSON: {}", e.getMessage());
            return "{}";
        }
    }
    
    /**
     * Convert object to pretty-printed JSON (for debugging)
     */
    public static String toPrettyJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert object to pretty JSON: {}", e.getMessage());
            return "{}";
        }
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Merge two JSON objects (shallow merge)
     */
    public static String mergeJson(String baseJson, String overrideJson) {
        Map<String, Object> base = parseStringObjectMap(baseJson);
        Map<String, Object> override = parseStringObjectMap(overrideJson);
        
        base.putAll(override);
        return toJson(base);
    }
    
    /**
     * Check if JSON string is valid
     */
    public static boolean isValidJson(String json) {
        if (isEmpty(json)) {
            return false;
        }
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Safe JSON string check
     */
    private static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty() || str.equals("null");
    }
    
    // ===== COMMON TYPE REFERENCES (for reuse) =====
    
    /**
     * Pre-defined TypeReferences for common patterns
     */
    public static class Types {
        public static final TypeReference<Map<String, String>> MAP_STRING_STRING = 
            new TypeReference<Map<String, String>>() {};
            
        public static final TypeReference<Map<String, Object>> MAP_STRING_OBJECT = 
            new TypeReference<Map<String, Object>>() {};
            
        public static final TypeReference<Map<String, List<String>>> MAP_STRING_LIST_STRING = 
            new TypeReference<Map<String, List<String>>>() {};
            
        public static final TypeReference<List<Map<String, Object>>> LIST_MAP_STRING_OBJECT = 
            new TypeReference<List<Map<String, Object>>>() {};
            
        public static final TypeReference<List<String>> LIST_STRING = 
            new TypeReference<List<String>>() {};
            
        public static final TypeReference<Map<String, Map<String, Object>>> MAP_STRING_MAP_STRING_OBJECT = 
            new TypeReference<Map<String, Map<String, Object>>>() {};
    }
}