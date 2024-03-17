package org.mvnsearch.spring.boot.nats.services;

import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.lang.Nullable;
import org.springframework.messaging.handler.annotation.reactive.MessageMappingMessageHandler;
import org.springframework.messaging.handler.invocation.reactive.HandlerMethodReturnValueHandler;
import org.springframework.util.RouteMatcher;

import java.util.ArrayList;
import java.util.List;

public class NatsServiceMessageHandler extends MessageMappingMessageHandler {

  private final List<Encoder<?>> encoders = new ArrayList<>();
  private NatsStrategies strategies = NatsStrategies.create();

  public void setEncoders(List<? extends Encoder<?>> encoders) {
    this.encoders.clear();
    this.encoders.addAll(encoders);
    this.strategies = this.strategies.mutate()
      .encoders(list -> {
        list.clear();
        list.addAll(encoders);
      })
      .build();
  }

  /**
   * Return the configured {@link #setEncoders(List) encoders}.
   */
  public List<? extends Encoder<?>> getEncoders() {
    return this.encoders;
  }

  @Override
  public void setDecoders(List<? extends Decoder<?>> decoders) {
    super.setDecoders(decoders);
    this.strategies = this.strategies.mutate()
      .decoders(list -> {
        list.clear();
        list.addAll(decoders);
      })
      .build();
  }

  @Override
  public void setRouteMatcher(@Nullable RouteMatcher routeMatcher) {
    super.setRouteMatcher(routeMatcher);
    this.strategies = this.strategies.mutate().routeMatcher(routeMatcher).build();
  }

  @Override
  public void setReactiveAdapterRegistry(ReactiveAdapterRegistry registry) {
    super.setReactiveAdapterRegistry(registry);
    this.strategies = this.strategies.mutate().reactiveAdapterStrategy(registry).build();
  }

  public void setNatsStrategies(NatsStrategies natsStrategies) {
    this.strategies = natsStrategies;
    this.encoders.clear();
    this.encoders.addAll(this.strategies.encoders());
    super.setDecoders(this.strategies.decoders());
    super.setRouteMatcher(this.strategies.routeMatcher());
    super.setReactiveAdapterRegistry(this.strategies.reactiveAdapterRegistry());
  }

  public NatsStrategies getNatsStrategies() {
    return this.strategies;
  }


  @Override
  public List<? extends HandlerMethodReturnValueHandler> initReturnValueHandlers() {
    List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>();
    handlers.add(new NatsServiceReturnValueHandler(this.encoders, getReactiveAdapterRegistry()));
    handlers.addAll(getReturnValueHandlerConfigurer().getCustomHandlers());
    return handlers;
  }

}
