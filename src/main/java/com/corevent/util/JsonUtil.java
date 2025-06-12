package com.corevent.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());
    
    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Error converting object to JSON", e);
        }
    }
    
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to object", e);
        }
    }
} 