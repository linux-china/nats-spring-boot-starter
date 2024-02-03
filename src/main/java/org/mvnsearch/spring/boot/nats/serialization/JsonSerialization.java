package org.mvnsearch.spring.boot.nats.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

public class JsonSerialization implements ObjectSerialization {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.findAndRegisterModules();
        objectMapper.setDefaultPrettyPrinter(new MinimalPrettyPrinter());
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Object convert(byte[] bytes, Class<?> targetClass) throws Exception {
        if (targetClass == int.class || targetClass == Integer.class) {
            return Integer.valueOf(bytesToString(bytes));
        } else if (targetClass == long.class || targetClass == Long.class) {
            return Long.valueOf(bytesToString(bytes));
        } else if (targetClass == double.class || targetClass == Double.class) {
            return Double.valueOf(bytesToString(bytes));
        } else if (targetClass == float.class || targetClass == Float.class) {
            return Float.valueOf(bytesToString(bytes));
        } else if (targetClass == boolean.class || targetClass == Boolean.class) {
            return Boolean.valueOf(bytesToString(bytes));
        } else if (targetClass == byte.class || targetClass == Byte.class) {
            return Byte.valueOf(bytesToString(bytes));
        } else if (targetClass == short.class || targetClass == Short.class) {
            return Short.valueOf(bytesToString(bytes));
        } else if (targetClass == String.class) {
            return new String(bytes, StandardCharsets.UTF_8);
        } else {
            return objectMapper.readValue(bytes, targetClass);
        }
    /*
        return switch (targetClass.getName()) {
            case "int", "java.lang.Integer" -> Integer.valueOf(text);
            case "long", "java.lang.Long" -> Long.valueOf(text);
            case "double", "java.lang.Double" -> Double.valueOf(text);
            case "float", "java.lang.Float" -> Float.valueOf(text);
            case "boolean", "java.lang.Boolean" -> Boolean.valueOf(text);
            case "byte", "java.lang.Byte" -> Byte.valueOf(text);
            case "short", "java.lang.Short" -> Short.valueOf(text);
            case "java.lang.String" -> text;
            default -> objectMapper.readValue(text, targetClass);
        };
     */
    }

    public byte[] toBytes(Object object) throws Exception {
        if (object instanceof String) {
            return ((String) object).getBytes(StandardCharsets.UTF_8);
        }
        return objectMapper.writeValueAsBytes(object);
    }

    public String bytesToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
