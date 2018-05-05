package org.mvnsearch.spring.boot.nats;

import io.nats.client.Connection;
import io.nats.client.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * NATS endpoint
 *
 * @author linux_china
 */
public class NatsEndpoint extends AbstractEndpoint<Map<String, Object>> {
    @Autowired
    private NatsProperties properties;
    @Autowired
    private Connection nats;
    @Autowired
    private NatsSubscriberAnnotationBeanPostProcessor postProcessor;

    public NatsEndpoint() {
        super("nats");
    }

    @Override
    public Map<String, Object> invoke() {
        Map<String, Object> info = new HashMap<>();
        info.put("connected", nats.isConnected());
        info.put("url", properties.getUrl());
        info.put("subjects", postProcessor.getSubscriptions().stream().map(Subscription::getSubject).collect(Collectors.toList()));
        info.put("stats", nats.getStats());
        return info;
    }
}
