package org.mvnsearch.spring.boot.nats.client;

import org.mvnsearch.spring.boot.nats.annotation.MessagingExchange;
import org.mvnsearch.spring.boot.nats.annotation.NatsExchange;
import org.mvnsearch.spring.boot.nats.annotation.ServiceExchange;
import reactor.core.publisher.Mono;

@NatsExchange(value = "nats://localhost:4222", path = "minmax")
public interface MathService {

  @ServiceExchange("min")
  Mono<Integer> min(String text);

  @ServiceExchange("max")
  Mono<Integer> max(String text);

  /**
   * send a message to topic
   *
   * @param text text
   */
  @MessagingExchange("post")
  void post(String text);
}
