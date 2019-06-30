package org.mvnsearch.spring.boot.nats.streaming;

import org.springframework.beans.factory.annotation.Autowired;
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
@EnableConfigurationProperties(NatsStreamingProperties.class)
public class NatsStreamingAutoConfiguration {

	@Autowired
	private NatsStreamingProperties properties;

	@Autowired
	private Connection nats;

	@Bean(destroyMethod = "close")
	public StreamingConnection natsStreaming() throws Exception {
		Options.Builder builder = new Options.Builder();
		builder.natsConn(nats);

		if (properties.getConnectWait() != null) {
			builder.connectWait(properties.getConnectWait());
		}
		
		if (properties.getAckTimeout() != null) {
			builder.pubAckWait(properties.getAckTimeout());
		}
		
		if (properties.getMaxPubAcksInFlight() != null ) {
			builder.maxPubAcksInFlight(properties.getMaxPubAcksInFlight());
		}

		return NatsStreaming.connect(properties.getClusterId(), properties.getClientId(), builder.build());
	}

	@Bean
	public NatsStreamingSubscriberAnnotationBeanPostProcessor natsStreamingSubscriberAnnotationBeanPostProcessor() {
		return new NatsStreamingSubscriberAnnotationBeanPostProcessor();
	}

	/*
	 * @Bean public NatsEndpoint natsEndpoint() { return new NatsEndpoint(); }
	 * 
	 * @Bean public NatsHealthIndicator natsHealthIndicator() { return new
	 * NatsHealthIndicator(); }
	 */
}
