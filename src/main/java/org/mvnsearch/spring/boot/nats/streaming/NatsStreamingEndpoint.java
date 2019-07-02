package org.mvnsearch.spring.boot.nats.streaming;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import io.nats.streaming.StreamingConnection;

/**
 * NATS Streaming endpoint
 *
 * @author wisni
 */
@Endpoint(id = "streaming")
public class NatsStreamingEndpoint {

    @Autowired
    private NatsStreamingProperties properties;

    @Autowired
    private StreamingConnection connection;

    @Autowired
    private NatsStreamingSubscriberAnnotationBeanPostProcessor postProcessor;

    @ReadOperation
    public Map<String, Object> invoke() {
        Map<String, Object> info = new HashMap<>();
        info.put("status", this.connection.getNatsConnection().getStatus().toString());
        info.put("url", this.connection.getNatsConnection().getConnectedUrl());
        info.put("clusterId", this.properties.getClusterId());
        info.put("clientId", this.properties.getClientId());
        info.put("subjects",
            this.postProcessor.getSubscriptions().keySet().stream().map(NatsStreamingSubscriber::subject).collect(Collectors.toList()));
        info.put("statistics", this.connection.getNatsConnection().getStatistics());

        return info;
    }
}
