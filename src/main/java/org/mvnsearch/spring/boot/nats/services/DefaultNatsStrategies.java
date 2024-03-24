package org.mvnsearch.spring.boot.nats.services;


import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.codec.*;
import org.springframework.lang.Nullable;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.RouteMatcher;
import org.springframework.util.SimpleRouteMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Default implementation of {@link NatsStrategies}.
 *
 * @author Rossen Stoyanchev
 * @author Brian Clozel
 * @since 5.2
 */
final class DefaultNatsStrategies implements NatsStrategies {

  private final List<Encoder<?>> encoders;

  private final List<Decoder<?>> decoders;

  private final RouteMatcher routeMatcher;

  private final ReactiveAdapterRegistry adapterRegistry;

  private DefaultNatsStrategies(List<Encoder<?>> encoders, List<Decoder<?>> decoders,
                                RouteMatcher routeMatcher, ReactiveAdapterRegistry adapterRegistry) {

    this.encoders = Collections.unmodifiableList(encoders);
    this.decoders = Collections.unmodifiableList(decoders);
    this.routeMatcher = routeMatcher;
    this.adapterRegistry = adapterRegistry;
  }


  @Override
  public List<Encoder<?>> encoders() {
    return this.encoders;
  }

  @Override
  public List<Decoder<?>> decoders() {
    return this.decoders;
  }

  @Override
  public RouteMatcher routeMatcher() {
    return this.routeMatcher;
  }

  @Override
  public ReactiveAdapterRegistry reactiveAdapterRegistry() {
    return this.adapterRegistry;
  }

  /**
   * Default implementation of {@link RSocketStrategies.Builder}.
   */
  static class DefaultNatsStrategiesBuilder implements NatsStrategies.Builder {

    private final List<Encoder<?>> encoders = new ArrayList<>();

    private final List<Decoder<?>> decoders = new ArrayList<>();

    private RouteMatcher routeMatcher;

    private ReactiveAdapterRegistry adapterRegistry = ReactiveAdapterRegistry.getSharedInstance();

    DefaultNatsStrategiesBuilder() {
      this.encoders.add(CharSequenceEncoder.allMimeTypes());
      this.encoders.add(new ByteBufferEncoder());
      this.encoders.add(new ByteArrayEncoder());
      this.encoders.add(new DataBufferEncoder());

      // Order of decoders may be significant for default data MimeType
      // selection in RSocketRequester.Builder
      this.decoders.add(StringDecoder.allMimeTypes());
      this.decoders.add(new ByteBufferDecoder());
      this.decoders.add(new ByteArrayDecoder());
      this.decoders.add(new DataBufferDecoder());
    }

    DefaultNatsStrategiesBuilder(NatsStrategies other) {
      this.encoders.addAll(other.encoders());
      this.decoders.addAll(other.decoders());
      this.routeMatcher = other.routeMatcher();
      this.adapterRegistry = other.reactiveAdapterRegistry();
    }


    @Override
    public Builder encoder(Encoder<?>... encoders) {
      this.encoders.addAll(Arrays.asList(encoders));
      return this;
    }

    @Override
    public Builder decoder(Decoder<?>... decoder) {
      this.decoders.addAll(Arrays.asList(decoder));
      return this;
    }

    @Override
    public Builder encoders(Consumer<List<Encoder<?>>> consumer) {
      consumer.accept(this.encoders);
      return this;
    }

    @Override
    public Builder decoders(Consumer<List<Decoder<?>>> consumer) {
      consumer.accept(this.decoders);
      return this;
    }

    @Override
    public Builder routeMatcher(@Nullable RouteMatcher routeMatcher) {
      this.routeMatcher = routeMatcher;
      return this;
    }

    @Override
    public Builder reactiveAdapterStrategy(@Nullable ReactiveAdapterRegistry registry) {
      this.adapterRegistry = registry;
      return this;
    }

    @Override
    public NatsStrategies build() {
      RouteMatcher matcher = (this.routeMatcher != null ? this.routeMatcher : initRouteMatcher());

      ReactiveAdapterRegistry registry = (this.adapterRegistry != null ?
        this.adapterRegistry : ReactiveAdapterRegistry.getSharedInstance());


      return new DefaultNatsStrategies(this.encoders, this.decoders, matcher, registry);
    }

    private RouteMatcher initRouteMatcher() {
      AntPathMatcher pathMatcher = new AntPathMatcher();
      pathMatcher.setPathSeparator(".");
      return new SimpleRouteMatcher(pathMatcher);
    }
  }

}
