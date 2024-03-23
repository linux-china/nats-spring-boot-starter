package org.mvnsearch.spring.boot.nats.services.codec;

import org.apache.avro.specific.SpecificRecord;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractDataBufferDecoder;
import org.springframework.core.codec.AbstractDecoder;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public class ApacheAvroDecoder extends AbstractDataBufferDecoder<SpecificRecord> {


  public ApacheAvroDecoder() {
    super(new MimeType("application", "avro"));
  }

  @Override
  public boolean canDecode(ResolvableType elementType, MimeType mimeType) {
    Class<?> clazz = elementType.toClass();
    return super.canDecode(elementType, mimeType) && SpecificRecord.class.isAssignableFrom(clazz);
  }

  @Override
  public SpecificRecord decode(DataBuffer buffer, ResolvableType targetType, MimeType mimeType, Map<String, Object> hints) throws DecodingException {
    Class<?> clazz = targetType.toClass();
    try {
      final Method fromByteBufferMethod = clazz.getMethod("fromByteBuffer", ByteBuffer.class);
      return (SpecificRecord) fromByteBufferMethod.invoke(null, buffer.asByteBuffer());
    } catch (Exception e) {
      throw new DecodingException("Avro record class must have 'fromByteBuffer' method", e);
    }
  }
}
