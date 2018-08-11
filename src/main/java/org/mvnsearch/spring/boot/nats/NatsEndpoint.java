package org.mvnsearch.spring.boot.nats;

import io.nats.client.Connection;
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
        info.put("status", nats.getStatus().toString());
        info.put("url", properties.getUrl());
        info.put("subjects", postProcessor.getSubscriptions().keySet().stream().map(NatsSubscriber::subject).collect(Collectors.toList()));
        info.put("statistics", nats.getStatistics());
        return info;
    }
}
