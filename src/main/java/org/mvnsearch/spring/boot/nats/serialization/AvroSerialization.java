package org.mvnsearch.spring.boot.nats.serialization;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class AvroSerialization implements ObjectSerialization {

  public Object convert(byte[] bytes, Class<?> targetClass) throws Exception {
    Method parseFromMethod = targetClass.getMethod("fromByteBuffer", ByteBuffer.class);
    return parseFromMethod.invoke(null, ByteBuffer.wrap(bytes));
  }

  public byte[] toBytes(Object object) throws Exception {
    Method toByteBufferMethod = object.getClass().getMethod("toByteBuffer");
    ByteBuffer byteBuffer = (ByteBuffer) toByteBufferMethod.invoke(object);
    if (byteBuffer.hasArray()) {
      return byteBuffer.array();
    } else {
      byte[] arr = new byte[byteBuffer.remaining()];
      byteBuffer.get(arr);
      return arr;
    }
  }
}
