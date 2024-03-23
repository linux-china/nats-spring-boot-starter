package org.mvnsearch.spring.boot.nats.services.codec;

import org.apache.avro.specific.SpecificRecord;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractEncoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public class ApacheAvroEncoder extends AbstractEncoder<SpecificRecord> {

  public ApacheAvroEncoder() {
    super(new MimeType("application", "avro"));
  }

  @Override
  public boolean canEncode(ResolvableType elementType, @Nullable MimeType mimeType) {
    Class<?> clazz = elementType.toClass();
    return super.canEncode(elementType, mimeType) && SpecificRecord.class.isAssignableFrom(clazz);
  }

  @Override
  public Flux<DataBuffer> encode(Publisher<? extends SpecificRecord> inputStream, DataBufferFactory bufferFactory, ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
    return Flux.from(inputStream).map((record) ->
      encodeValue(record, bufferFactory, elementType, mimeType, hints));
  }

  @Override
  public DataBuffer encodeValue(SpecificRecord record, DataBufferFactory bufferFactory, ResolvableType valueType, MimeType mimeType, Map<String, Object> hints) {
    try {
      final Method toByteBufferMethod = record.getClass().getMethod("toByteBuffer");
      final ByteBuffer buffer = (ByteBuffer) toByteBufferMethod.invoke(record);
      return DefaultDataBufferFactory.sharedInstance.wrap(buffer);
    } catch (Exception e) {
      throw new IllegalArgumentException("Avro record class must have 'toByteBuffer' method");
    }
  }
}
