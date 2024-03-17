package org.mvnsearch.spring.boot.nats;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mvnsearch.spring.boot.nats.services.NatsStrategies;
import org.mvnsearch.spring.boot.nats.services.NatsStrategiesCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.codec.ByteArrayDecoder;
import org.springframework.core.codec.ByteArrayEncoder;
import org.springframework.core.codec.CharSequenceEncoder;
import org.springframework.core.codec.StringDecoder;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.ClassUtils;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;

@AutoConfiguration(after = JacksonAutoConfiguration.class)
public class NatsStrategiesAutoConfiguration {
  private static final String PATHPATTERN_ROUTEMATCHER_CLASS = "org.springframework.web.util.pattern.PathPatternRouteMatcher";

  @Bean
  @ConditionalOnMissingBean
  public NatsStrategies natsStrategies(ObjectProvider<NatsStrategiesCustomizer> customizers) {
    NatsStrategies.Builder builder = NatsStrategies.builder();
    if (ClassUtils.isPresent(PATHPATTERN_ROUTEMATCHER_CLASS, null)) {
      builder.routeMatcher(new PathPatternRouteMatcher());
    }
    customizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
    return builder.build();
  }


  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(ObjectMapper.class)
  protected static class JacksonJsonStrategyConfiguration {

    private static final MediaType[] SUPPORTED_TYPES = {
      MediaType.APPLICATION_JSON, new MediaType("application", "*+json"),
      MediaType.TEXT_PLAIN, new MediaType("text", "*")
    };

    @Bean
    @Order(1)
    @ConditionalOnBean(ObjectMapper.class)
    public NatsStrategiesCustomizer jacksonJsonNatsStrategyCustomizer(ObjectMapper objectMapper) {
      return (strategy) -> {
        strategy.decoder(new Jackson2JsonDecoder(objectMapper, SUPPORTED_TYPES));
        strategy.encoder(new Jackson2JsonEncoder(objectMapper, SUPPORTED_TYPES));
      };
    }
  }

  @Configuration(proxyBeanMethods = false)
  protected static class TextStrategyConfiguration {
    @Bean
    @Order(1)
    public NatsStrategiesCustomizer textNatsStrategyCustomizer(ObjectMapper objectMapper) {
      return (strategy) -> {
        strategy.decoder(StringDecoder.textPlainOnly());
        strategy.encoder(CharSequenceEncoder.textPlainOnly());
      };
    }
  }

  @Configuration(proxyBeanMethods = false)
  protected static class BinaryStrategyConfiguration {
    @Bean
    @ConditionalOnBean(ObjectMapper.class)
    public NatsStrategiesCustomizer binaryNatsStrategyCustomizer(ObjectMapper objectMapper) {
      return (strategy) -> {
        strategy.decoder(new ByteArrayDecoder());
        strategy.encoder(new ByteArrayEncoder());
      };
    }
  }
}
