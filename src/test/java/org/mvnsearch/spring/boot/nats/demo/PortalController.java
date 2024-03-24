package org.mvnsearch.spring.boot.nats.demo;

import org.mvnsearch.spring.boot.nats.core.NatsTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PortalController {
  @Autowired
  private NatsTemplate natsTemplate;
  @GetMapping("/")
  public String index() {
    return "Hello Nats";
  }
}
