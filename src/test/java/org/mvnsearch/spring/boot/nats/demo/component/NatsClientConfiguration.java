package org.mvnsearch.spring.boot.nats.demo.component;

import io.nats.client.Connection;
import org.mvnsearch.spring.boot.nats.client.NatsExchangeProxyFactory;
import org.mvnsearch.spring.boot.nats.client.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NatsClientConfiguration {

    @Bean
    public UserService userService(Connection nc) {
        return NatsExchangeProxyFactory.buildStub(nc, UserService.class);
    }
}
