package org.mvnsearch.spring.boot.nats.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

public class JsonUtil {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static Object convert(String text, Class<?> targetClass) throws Exception {
    if (targetClass == int.class || targetClass == Integer.class) {
      return Integer.valueOf(text);
    } else if (targetClass == long.class || targetClass == Long.class) {
      return Long.valueOf(text);
    } else if (targetClass == double.class || targetClass == Double.class) {
      return Double.valueOf(text);
    } else if (targetClass == float.class || targetClass == Float.class) {
      return Float.valueOf(text);
    } else if (targetClass == boolean.class || targetClass == Boolean.class) {
      return Boolean.valueOf(text);
    } else if (targetClass == byte.class || targetClass == Byte.class) {
      return Byte.valueOf(text);
    } else if (targetClass == short.class || targetClass == Short.class) {
      return Short.valueOf(text);
    } else if (targetClass == String.class) {
      return text;
    } else {
      return objectMapper.readValue(text, targetClass);
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

  public static String toJson(Object object) throws Exception {
    if (object instanceof String) {
      return (String) object;
    }
    return objectMapper.writeValueAsString(object);
  }

  public static byte[] toJsonBytes(Object object) throws Exception {
    if (object instanceof String) {
      return ((String) object).getBytes(StandardCharsets.UTF_8);
    }
    return objectMapper.writeValueAsBytes(object);
  }
}
