package org.mvnsearch.spring.boot.nats.client;

import io.nats.client.Connection;
import io.nats.client.Nats;
import org.junit.jupiter.api.Test;
import org.mvnsearch.spring.boot.nats.configuration.NatsExchangeProxyFactory;

public class MathServiceTest {

    @Test
    public void testServiceCall() throws Exception {
        Connection nc = Nats.connect("nats://localhost:4222");
        MathService userService = NatsExchangeProxyFactory.buildStub(nc, MathService.class);
        Integer min = userService.min("1,2").block();
        System.out.println(min);
    }
}
