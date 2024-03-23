package org.mvnsearch.spring.boot.nats.services.codec;

import com.google.protobuf.MessageLite;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractEncoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

import java.util.Map;

public class ProtobufEncoder extends AbstractEncoder<MessageLite> {

  public ProtobufEncoder() {
    super(new MimeType("application", "protobuf"));
  }

  @Override
  public boolean canEncode(ResolvableType elementType, @Nullable MimeType mimeType) {
    Class<?> clazz = elementType.toClass();
    return super.canEncode(elementType, mimeType) && MessageLite.class.isAssignableFrom(clazz);
  }

  @Override
  public Flux<DataBuffer> encode(Publisher<? extends MessageLite> inputStream, DataBufferFactory bufferFactory, ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
    return Flux.from(inputStream).map((record) ->
      encodeValue(record, bufferFactory, elementType, mimeType, hints));
  }

  @Override
  public DataBuffer encodeValue(MessageLite msg, DataBufferFactory bufferFactory, ResolvableType valueType, MimeType mimeType, Map<String, Object> hints) {
    try {
      final byte[] bytes = msg.toByteArray();
      return DefaultDataBufferFactory.sharedInstance.wrap(bytes);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to encode Protobuf message", e);
    }
  }
}
