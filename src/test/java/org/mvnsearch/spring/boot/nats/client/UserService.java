package org.mvnsearch.spring.boot.nats.client;

import org.mvnsearch.spring.boot.nats.annotation.NatsExchange;
import org.mvnsearch.spring.boot.nats.annotation.ServiceExchange;
import org.mvnsearch.spring.boot.nats.model.User;
import reactor.core.publisher.Mono;

@NatsExchange(value = "nats://localhost:4222", path = "UserService", contentType = "application/json")
public interface UserService {
  @ServiceExchange("hello")
  Mono<String> hello(User user);
}
