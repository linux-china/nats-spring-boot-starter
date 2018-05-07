package org.mvnsearch.spring.boot.nats;

import io.nats.client.Connection;
import io.nats.client.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * NATS endpoint
 *
 * @author linux_china
 */
@Endpoint(id = "nats")
public class NatsEndpoint {
    @Autowired
    private NatsProperties properties;
    @Autowired
    private Connection nats;
    @Autowired
    private NatsSubscriberAnnotationBeanPostProcessor postProcessor;

    @ReadOperation
    public Map<String, Object> invoke() {
        Map<String, Object> info = new HashMap<>();
        info.put("connected", nats.isConnected());
        info.put("url", properties.getUrl());
        info.put("subjects", postProcessor.getSubscriptions().stream().map(Subscription::getSubject).collect(Collectors.toList()));
        info.put("stats", nats.getStats());
        return info;
    }
}
