package org.mvnsearch.spring.boot.nats;

import io.nats.client.Connection;
import io.nats.client.Nats;
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
            Nats.ConnState state = nats.getState();
            builder.up().withDetail("url", nats.getConnectedUrl()).withDetail("status", state.name());
        } catch (Exception e) {
            builder.down(e);
        }
    }
}