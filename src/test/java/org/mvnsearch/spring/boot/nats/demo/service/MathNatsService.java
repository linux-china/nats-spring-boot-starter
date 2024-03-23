package org.mvnsearch.spring.boot.nats.demo.service;

import org.mvnsearch.spring.boot.nats.annotation.NatsService;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.Map;

@Controller
@MessageMapping("minmax")
@NatsService(name = "minmax", version = "0.0.1", description = "min/max service for numbers split by comma")
public class MathNatsService {

  @MessageMapping("min")
  public Mono<String> min(@Payload String body, @Headers Map<String, String> headers) {
    int min = Integer.MAX_VALUE;
    String[] input = body.split(",");
    for (String n : input) {
      min = Math.min(min, Integer.parseInt(n));
    }
    return Mono.just(String.valueOf(min));
  }

  @MessageMapping("max")
  public Mono<String> max(@Payload String body) {
    int max = Integer.MIN_VALUE;
    String[] input = body.split(",");
    for (String n : input) {
      max = Math.max(max, Integer.parseInt(n));
    }
    return Mono.just(String.valueOf(max));
  }
}
