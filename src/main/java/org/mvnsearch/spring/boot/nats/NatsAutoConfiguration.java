package org.mvnsearch.spring.boot.nats;

import io.nats.client.Connection;
import io.nats.client.Nats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * NATS auto configuration
 *
 * @author linux_china
 */
@Configuration
@EnableConfigurationProperties(NatsProperties.class)
public class NatsAutoConfiguration {

    @Autowired
    private NatsProperties properties;

    @Bean
    public Connection nats() throws Exception {
        return Nats.connect(properties.getUrl());
    }
}
