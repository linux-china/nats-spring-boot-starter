package org.mvnsearch.spring.boot.nats.services.codec;

import com.google.protobuf.MessageLite;
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

public class ProtobufDecoder extends AbstractDataBufferDecoder<MessageLite> {


  public ProtobufDecoder() {
    super(new MimeType("application", "protobuf"));
  }

  @Override
  public boolean canDecode(ResolvableType elementType, MimeType mimeType) {
    Class<?> clazz = elementType.toClass();
    return super.canDecode(elementType, mimeType) && MessageLite.class.isAssignableFrom(clazz);
  }

  @Override
  public MessageLite decode(DataBuffer buffer, ResolvableType targetType, MimeType mimeType, Map<String, Object> hints) throws DecodingException {
    Class<?> clazz = targetType.toClass();
    try {
      final Method fromByteBufferMethod = clazz.getMethod("parseFrom", ByteBuffer.class);
      return (MessageLite) fromByteBufferMethod.invoke(null, buffer.asByteBuffer());
    } catch (Exception e) {
      throw new DecodingException("Failed to decode Protobuf message", e);
    }
  }

}
