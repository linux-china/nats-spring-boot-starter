package org.mvnsearch.spring.boot.nats;

import org.mvnsearch.spring.boot.nats.configuration.NatsDurableBeanPostProcessor;
import org.mvnsearch.spring.boot.nats.configuration.NatsServiceBeanPostProcessor;
import org.mvnsearch.spring.boot.nats.configuration.NatsSubscriberAnnotationBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * NATS auto configuration
 *
 * @author linux_china
 */
@Configuration
public class NatsAutoConfiguration {

  @Bean
  public NatsSubscriberAnnotationBeanPostProcessor natsSubscriberAnnotationBeanPostProcessor() {
    return new NatsSubscriberAnnotationBeanPostProcessor();
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
  public NatsHealthIndicator natsHealthIndicator() {
    return new NatsHealthIndicator();
  }
}
