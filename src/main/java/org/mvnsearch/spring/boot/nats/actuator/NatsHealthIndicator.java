package org.mvnsearch.spring.boot.nats.actuator;

import io.nats.client.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * NATS health indicator
 *
 * @author linux_china
 */
public class NatsHealthIndicator extends AbstractHealthIndicator {
  @Autowired
  private Connection nc;

  @Override
  protected void doHealthCheck(Health.Builder builder) {
    try {
      Connection.Status status = nc.getStatus();
      builder.up().withDetail("url", nc.getConnectedUrl()).withDetail("status", status.toString());
    } catch (Exception e) {
      builder.down(e);
    }
  }
}
