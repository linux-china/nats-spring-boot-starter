package org.mvnsearch.spring.boot.nats.services;


import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.lang.Nullable;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MimeType;
import org.springframework.util.RouteMatcher;
import org.springframework.util.SimpleRouteMatcher;

import java.util.List;
import java.util.function.Consumer;

/**
 * Access to strategies for use by NATS request-reply model.
 */
public interface NatsStrategies {

  /**
   * Return the configured {@link org.springframework.messaging.rsocket.RSocketStrategies.Builder#encoder(Encoder[]) encoders}.
   *
   * @see #encoder(ResolvableType, MimeType)
   */
  List<Encoder<?>> encoders();

  /**
   * Find a compatible Encoder for the given element type.
   *
   * @param elementType the element type to match
   * @param mimeType    the MimeType to match
   * @param <T>         for casting the Encoder to the expected element type
   * @return the matching Encoder
   * @throws IllegalArgumentException if no matching Encoder is found
   */
  @SuppressWarnings("unchecked")
  default <T> Encoder<T> encoder(ResolvableType elementType, @Nullable MimeType mimeType) {
    for (Encoder<?> encoder : encoders()) {
      if (encoder.canEncode(elementType, mimeType)) {
        return (Encoder<T>) encoder;
      }
    }
    throw new IllegalArgumentException("No encoder for " + elementType);
  }

  /**
   * Return the configured {@link org.springframework.messaging.rsocket.RSocketStrategies.Builder#decoder(Decoder[]) decoders}.
   *
   * @see #decoder(ResolvableType, MimeType)
   */
  List<Decoder<?>> decoders();

  /**
   * Find a compatible Decoder for the given element type.
   *
   * @param elementType the element type to match
   * @param mimeType    the MimeType to match
   * @param <T>         for casting the Decoder to the expected element type
   * @return the matching Decoder
   * @throws IllegalArgumentException if no matching Decoder is found
   */
  @SuppressWarnings("unchecked")
  default <T> Decoder<T> decoder(ResolvableType elementType, @Nullable MimeType mimeType) {
    for (Decoder<?> decoder : decoders()) {
      if (decoder.canDecode(elementType, mimeType)) {
        return (Decoder<T>) decoder;
      }
    }
    throw new IllegalArgumentException("No decoder for " + elementType);
  }

  /**
   * Return the configured {@link org.springframework.messaging.rsocket.RSocketStrategies.Builder#routeMatcher(RouteMatcher)}.
   */
  RouteMatcher routeMatcher();

  /**
   * Return the configured
   * {@link org.springframework.messaging.rsocket.RSocketStrategies.Builder#reactiveAdapterStrategy(ReactiveAdapterRegistry) reactiveAdapterRegistry}.
   */
  ReactiveAdapterRegistry reactiveAdapterRegistry();

  /**
   * Return a builder to create a new {@link org.springframework.messaging.rsocket.RSocketStrategies} instance
   * replicated from the current instance.
   */
  default NatsStrategies.Builder mutate() {
    return new DefaultNatsStrategies.DefaultNatsStrategiesBuilder(this);
  }


  /**
   * Create an {@code RSocketStrategies} instance with default settings.
   * Equivalent to {@code RSocketStrategies.builder().build()}. See individual
   * builder methods for details on default settings.
   */
  static NatsStrategies create() {
    return new DefaultNatsStrategies.DefaultNatsStrategiesBuilder().build();
  }

  /**
   * Return a builder to prepare a new {@code RSocketStrategies} instance.
   * The builder applies default settings, see individual builder methods for
   * details.
   */
  static NatsStrategies.Builder builder() {
    return new DefaultNatsStrategies.DefaultNatsStrategiesBuilder();
  }


  /**
   * The builder options for creating {@code RSocketStrategies}.
   */
  interface Builder {

    /**
     * Append to the list of encoders to use for serializing Objects to the
     * data or metadata of a {@link Payload}.
     * <p>By default this is initialized with encoders for {@code String},
     * {@code byte[]}, {@code ByteBuffer}, and {@code DataBuffer}.
     */
    NatsStrategies.Builder encoder(Encoder<?>... encoder);

    /**
     * Apply the consumer to the list of configured encoders, immediately.
     */
    NatsStrategies.Builder encoders(Consumer<List<Encoder<?>>> consumer);

    /**
     * Append to the list of decoders to use for de-serializing Objects from
     * the data or metadata of a {@link Payload}.
     * <p>By default this is initialized with decoders for {@code String},
     * {@code byte[]}, {@code ByteBuffer}, and {@code DataBuffer}.
     */
    NatsStrategies.Builder decoder(Decoder<?>... decoder);

    /**
     * Apply the consumer to the list of configured decoders, immediately.
     */
    NatsStrategies.Builder decoders(Consumer<List<Decoder<?>>> consumer);

    /**
     * Configure a {@code RouteMatcher} for matching routes to message
     * handlers based on route patterns. This option is applicable to
     * client or server responders.
     * <p>By default, {@link SimpleRouteMatcher} is used, backed by
     * {@link AntPathMatcher} with "." as separator. For better
     * efficiency consider switching to {@code PathPatternRouteMatcher} from
     * {@code spring-web} instead.
     */
    NatsStrategies.Builder routeMatcher(@Nullable RouteMatcher routeMatcher);

    /**
     * Configure the registry for reactive type support. This can be used
     * to adapt to, and/or determine the semantics of a given
     * {@link org.reactivestreams.Publisher Publisher}.
     * <p>By default this {@link ReactiveAdapterRegistry#getSharedInstance()}.
     */
    NatsStrategies.Builder reactiveAdapterStrategy(@Nullable ReactiveAdapterRegistry registry);

    /**
     * Build the {@code RSocketStrategies} instance.
     */
    NatsStrategies build();
  }

}
