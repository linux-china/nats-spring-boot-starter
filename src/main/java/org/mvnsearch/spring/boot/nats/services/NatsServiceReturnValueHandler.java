package org.mvnsearch.spring.boot.nats.services;

import io.nats.client.impl.NatsMessage;
import io.netty.buffer.ByteBuf;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.reactive.AbstractEncoderMethodReturnValueHandler;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class NatsServiceReturnValueHandler extends AbstractEncoderMethodReturnValueHandler {

  public static final String RESPONSE_HEADER = "replyResponse";

  protected NatsServiceReturnValueHandler(List<Encoder<?>> encoders, ReactiveAdapterRegistry registry) {
    super(encoders, registry);
  }

  @Override
  protected Mono<Void> handleEncodedContent(Flux<DataBuffer> encodedContent, MethodParameter returnType, Message<?> message) {
    AtomicReference<Mono<io.nats.client.Message>> responseRef = getResponseReference(message);
    Assert.notNull(responseRef, "Missing '" + RESPONSE_HEADER + "'");
    responseRef.set(mergeDataBuffers(encodedContent).map(bytes -> NatsMessage.builder()
      .data(bytes)
      .subject(message.getHeaders().get("reply-to", String.class))
      .build()));
    return Mono.empty();
  }

  @Override
  protected Mono<Void> handleNoContent(MethodParameter returnType, Message<?> message) {
    return Mono.empty();
  }

  private AtomicReference<Mono<io.nats.client.Message>> getResponseReference(Message<?> message) {
    Object headerValue = message.getHeaders().get(RESPONSE_HEADER);
    Assert.state(headerValue == null || headerValue instanceof AtomicReference, "Expected AtomicReference");
    return (AtomicReference<Mono<io.nats.client.Message>>) headerValue;
  }

  Mono<byte[]> mergeDataBuffers(Flux<DataBuffer> dataBufferFlux) {
    return DataBufferUtils.join(dataBufferFlux)
      .map(dataBuffer -> {
        byte[] bytes = new byte[dataBuffer.readableByteCount()];
        dataBuffer.read(bytes);
        DataBufferUtils.release(dataBuffer);
        return bytes;
      });
  }

  static ByteBuf asByteBuf(DataBuffer buffer) {
    return NettyDataBufferFactory.toByteBuf(buffer);
  }

  private static ByteBuffer asByteBuffer(DataBuffer dataBuffer) {
    if (dataBuffer instanceof DefaultDataBuffer) {
      return ((DefaultDataBuffer) dataBuffer).getNativeBuffer();
    } else {
      return dataBuffer.asByteBuffer();
    }
  }
}
