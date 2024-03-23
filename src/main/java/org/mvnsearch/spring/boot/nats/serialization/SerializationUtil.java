package org.mvnsearch.spring.boot.nats.serialization;

import com.google.protobuf.MessageLite;
import org.apache.avro.specific.SpecificRecord;

import java.nio.charset.StandardCharsets;

public class SerializationUtil {
  private static final JsonSerialization json = new JsonSerialization();
  private static ProtobufSerialization protobuf = null;
  private static AvroSerialization avro = null;

  static {
    try {
      Class.forName("com.google.protobuf.MessageLite");
      protobuf = new ProtobufSerialization();
    } catch (Exception ignore) {

    }
    try {
      Class.forName("org.apache.avro.specific.SpecificRecord");
      avro = new AvroSerialization();
    } catch (Exception ignore) {

    }
  }

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
    } else if (protobuf != null && targetClass.isAssignableFrom(MessageLite.class)) {
      return protobuf.convert(bytes, targetClass);
    } else if (avro != null && targetClass.isAssignableFrom(SpecificRecord.class)) {
      return avro.convert(bytes, targetClass);
    } else {
      return json.convert(bytes, targetClass);
    }
  }

  public static byte[] toBytes(Object object, String contentType) throws Exception {
    if (contentType.startsWith("text/")) {
      return object.toString().getBytes(StandardCharsets.UTF_8);
    } else if (protobuf != null && object instanceof MessageLite) {
      return protobuf.toBytes(object);
    } else if (avro != null && object instanceof SpecificRecord) {
      return avro.toBytes(object);
    } else {
      return json.toBytes(object);
    }
  }
}
