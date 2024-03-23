package org.mvnsearch.spring.boot.nats.serialization;

import com.google.protobuf.Message;

import java.nio.charset.StandardCharsets;

public class SerializationUtil {
  private static final JsonSerialization json = new JsonSerialization();
  private static final ProtobufSerialization protobuf = new ProtobufSerialization();

  public static Object convert(byte[] bytes, Class<?> targetClass, String contentType) throws Exception {
    if (contentType.startsWith("text/")) {
      String text = new String(bytes, StandardCharsets.UTF_8);
      if (targetClass.isAssignableFrom(String.class)) {
        return text;
      } else if (targetClass.isAssignableFrom(Integer.class)) {
        return Integer.parseInt(text);
      } else if (targetClass.isAssignableFrom(Long.class)) {
        return Long.parseLong(text);
      } else if (targetClass.isAssignableFrom(Double.class)) {
        return Double.parseDouble(text);
      } else if (targetClass.isAssignableFrom(Float.class)) {
        return Float.parseFloat(text);
      } else if (targetClass.isAssignableFrom(Boolean.class)) {
        return Boolean.parseBoolean(text);
      } else {
        return json.convert(bytes, targetClass);
      }
    } else if (targetClass.isAssignableFrom(Message.class)) {
      return protobuf.convert(bytes, targetClass);
    } else {
      return json.convert(bytes, targetClass);
    }
  }

  public static byte[] toBytes(Object object, String contentType) throws Exception {
    if (contentType.startsWith("text/")) {
      return object.toString().getBytes(StandardCharsets.UTF_8);
    } else if (object instanceof Message) {
      return protobuf.toBytes(object);
    } else {
      return json.toBytes(object);
    }
  }
}
