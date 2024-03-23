package org.mvnsearch.spring.boot.nats.serialization;

import com.google.protobuf.Message;

import java.nio.charset.StandardCharsets;

public class SerializationUtil {
  private static final JsonSerialization json = new JsonSerialization();
  private static final ProtobufSerialization protobuf = new ProtobufSerialization();

  public static Object convert(byte[] bytes, Class<?> targetClass, String contentType) throws Exception {
    if (targetClass == String.class || contentType.startsWith("text/")) {
      return new String(bytes, StandardCharsets.UTF_8);
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
