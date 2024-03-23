package org.mvnsearch.spring.boot.nats;

import io.nats.client.Connection;
import io.nats.spring.boot.autoconfigure.NatsProperties;
import org.mvnsearch.spring.boot.nats.configuration.AppInstanceOnlyMessageHandlerBeanPostProcessor;
import org.mvnsearch.spring.boot.nats.configuration.NatsDurableBeanPostProcessor;
import org.mvnsearch.spring.boot.nats.configuration.NatsServiceBeanPostProcessor;
import org.mvnsearch.spring.boot.nats.configuration.NatsSubscriberAnnotationBeanPostProcessor;
import org.mvnsearch.spring.boot.nats.services.NatsMessageHandlerCustomizer;
import org.mvnsearch.spring.boot.nats.services.NatsServiceMessageHandler;
import org.mvnsearch.spring.boot.nats.services.NatsStrategies;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.messaging.ReactiveMessageHandler;

import java.nio.charset.StandardCharsets;

/**
 * NATS auto configuration
 *
 * @author linux_china
 */
@Configuration
public class NatsAutoConfiguration {

  @Bean
  public MessagingNats messagingNats(Connection nc, ReactiveMessageHandler messageHandler, NatsStrategies strategies) {
    return new MessagingNats(new MediaType("text", "plain", StandardCharsets.UTF_8), nc, messageHandler, strategies);
  }

  @Bean
  public AppInstanceOnlyMessageHandlerBeanPostProcessor appInstanceOnlyMessageHandlerBeanPostProcessor() {
    return new AppInstanceOnlyMessageHandlerBeanPostProcessor();
  }

  @Bean
  public NatsSubscriberAnnotationBeanPostProcessor natsSubscriberAnnotationBeanPostProcessor() {
    return new NatsSubscriberAnnotationBeanPostProcessor();
  }

  @Bean
  public NatsServiceMessageHandler natsServiceMessageHandler(NatsStrategies natsStrategies,
                                                             ObjectProvider<NatsMessageHandlerCustomizer> customizers) {
    NatsServiceMessageHandler messageHandler = new NatsServiceMessageHandler();
    messageHandler.setNatsStrategies(natsStrategies);
    customizers.orderedStream().forEach((customizer) -> customizer.customize(messageHandler));
    return messageHandler;
  }

  @Bean
  public NatsServiceBeanPostProcessor natsServiceBeanPostProcessor() {
    return new NatsServiceBeanPostProcessor();
  }

  @Bean
  public NatsDurableBeanPostProcessor natsDurableBeanPostProcessor() {
    return new NatsDurableBeanPostProcessor();
  }

  @Bean
  public NatsActuatorEndpoint natsActuatorEndpoint(NatsProperties natsProperties) {
    return new NatsActuatorEndpoint(natsProperties);
  }

  @Bean
  public NatsHealthIndicator natsHealthIndicator() {
    return new NatsHealthIndicator();
  }
}
