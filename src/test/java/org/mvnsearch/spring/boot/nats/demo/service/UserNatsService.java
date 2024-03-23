package org.mvnsearch.spring.boot.nats.demo.service;

import org.mvnsearch.spring.boot.nats.annotation.NatsService;
import org.mvnsearch.spring.boot.nats.model.User;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@MessageMapping("UserService")
@NatsService(name = "UserService", version = "0.0.1", description = "user service")
public class UserNatsService {
  @MessageMapping("hello")
  public Mono<String> hello(@Payload User user) {
    return Mono.just("Hello " + user.getNick());
  }
}
