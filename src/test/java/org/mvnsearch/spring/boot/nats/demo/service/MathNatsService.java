package org.mvnsearch.spring.boot.nats.demo.service;

import io.nats.service.ServiceMessage;
import org.mvnsearch.spring.boot.nats.annotation.NatsService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@MessageMapping("minmax")
@NatsService(name = "minmax", version = "0.0.1", description = "min/max service for numbers split by comma")
public class MathNatsService {

  @MessageMapping("min")
  public int min(ServiceMessage msg) {
    int min = Integer.MAX_VALUE;
    String[] input = new String(msg.getData()).split(",");
    for (String n : input) {
      min = Math.min(min, Integer.parseInt(n));
    }
    return min;
  }

  @MessageMapping("max")
  public int max(ServiceMessage msg) {
    int max = Integer.MIN_VALUE;
    String[] input = new String(msg.getData()).split(",");
    for (String n : input) {
      max = Math.max(max, Integer.parseInt(n));
    }
    return max;
  }
}
