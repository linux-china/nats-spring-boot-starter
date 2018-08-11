package org.mvnsearch.spring.boot.nats;

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
    private Connection nats;

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try {
            Connection.Status status = nats.getStatus();
            builder.up().withDetail("url", nats.getConnectedUrl()).withDetail("status", status.toString());
        } catch (Exception e) {
            builder.down(e);
        }
    }
}
