package org.mvnsearch.spring.boot.nats.streaming;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.nats.client.Connection;
import io.nats.streaming.NatsStreaming;
import io.nats.streaming.Options;
import io.nats.streaming.StreamingConnection;

/**
 * NATS Streaming auto configuration
 *
 * @author wisni
 */
@Configuration
@ConditionalOnProperty(
    prefix = "nats.streaming",
    value = "enable",
    matchIfMissing = false)
@EnableConfigurationProperties(NatsStreamingProperties.class)
public class NatsStreamingAutoConfiguration {

    @Autowired
    private NatsStreamingProperties properties;

    @Autowired
    private Connection nats;

    @Bean(destroyMethod = "close")
    public StreamingConnection natsStreaming() throws Exception {
        Options.Builder builder = new Options.Builder();
        builder.natsConn(this.nats);

        if (this.properties.getConnectWait() != null) {
            builder.connectWait(this.properties.getConnectWait());
        }

        if (this.properties.getAckTimeout() != null) {
            builder.pubAckWait(this.properties.getAckTimeout());
        }

        if (this.properties.getMaxPubAcksInFlight() != null) {
            builder.maxPubAcksInFlight(this.properties.getMaxPubAcksInFlight());
        }

        return NatsStreaming.connect(this.properties.getClusterId(), this.properties.getClientId(), builder.build());
    }

    @Bean
    public NatsStreamingSubscriberAnnotationBeanPostProcessor natsStreamingSubscriberAnnotationBeanPostProcessor() {
        return new NatsStreamingSubscriberAnnotationBeanPostProcessor();
    }

    @Bean
    public NatsStreamingEndpoint natsStreamingEndpoint() {
        return new NatsStreamingEndpoint();
    }

}
