package org.mvnsearch.spring.boot.nats.services;

import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractEncoder;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class StringEncoder extends AbstractEncoder<byte[]> {
  public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  public StringEncoder(MimeType... mimeTypes) {
    super(mimeTypes);
  }

  @Override
  public boolean canEncode(ResolvableType elementType, @Nullable MimeType mimeType) {
    Class<?> clazz = elementType.toClass();
    return (super.canEncode(elementType, mimeType) || mimeType.getType().startsWith("text/"))
      && String.class.isAssignableFrom(clazz);
  }

  @Override
  public Flux<DataBuffer> encode(Publisher<? extends byte[]> inputStream, DataBufferFactory bufferFactory, ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
    return Flux.from(inputStream).map((byte[] bytes) ->
      encodeValue(bytes, bufferFactory, elementType, mimeType, hints));
  }

  @Override
  public DataBuffer encodeValue(byte[] bytes, DataBufferFactory bufferFactory, ResolvableType valueType, MimeType mimeType, Map<String, Object> hints) {
    DataBuffer dataBuffer = bufferFactory.wrap(bytes);
    if (logger.isDebugEnabled() && !Hints.isLoggingSuppressed(hints)) {
      String logPrefix = Hints.getLogPrefix(hints);
      logger.debug(logPrefix + "Writing " + dataBuffer.readableByteCount() + " bytes");
    }
    return dataBuffer;
  }

  @Override
  public List<MimeType> getEncodableMimeTypes(ResolvableType elementType) {
    return super.getEncodableMimeTypes(elementType);
  }

  public static StringEncoder textPlainOnly() {
    return new StringEncoder(new MimeType("text", "plain", DEFAULT_CHARSET));
  }
}
