package com.numbericsuserportal.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Converter {

    private final static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());  // Register Java 8 time module
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static String toJson(Object data) throws JsonProcessingException {
        return data!=null ? mapper.writeValueAsString(data) : null;
    }
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return json != null ? mapper.readValue(json, clazz) : null;
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing JSON to " + clazz.getSimpleName(), e);
        }
    }
}
