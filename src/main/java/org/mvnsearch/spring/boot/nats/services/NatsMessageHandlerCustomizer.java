package org.mvnsearch.spring.boot.nats.services;

@FunctionalInterface
public interface NatsMessageHandlerCustomizer {

  void customize(NatsServiceMessageHandler messageHandler);

}
