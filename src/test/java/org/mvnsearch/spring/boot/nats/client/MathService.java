package org.mvnsearch.spring.boot.nats.client;

import org.mvnsearch.spring.boot.nats.annotation.NatsExchange;
import org.mvnsearch.spring.boot.nats.annotation.ServiceExchange;
import reactor.core.publisher.Mono;

@NatsExchange("nats://localhost:4222")
public interface MathService {

  @ServiceExchange("minmax.min")
  Mono<Integer> min(String text);

  @ServiceExchange("minmax.max")
   Mono<Integer> max(String text);

}
