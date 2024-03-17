package org.mvnsearch.spring.boot.nats.services;

@java.lang.FunctionalInterface
public interface NatsStrategiesCustomizer {
  void customize(NatsStrategies.Builder strategies);
}
