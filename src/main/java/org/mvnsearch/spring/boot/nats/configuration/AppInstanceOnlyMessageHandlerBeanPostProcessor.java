package org.mvnsearch.spring.boot.nats.configuration;


import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.MessageHandler;
import org.mvnsearch.spring.boot.nats.AppInstanceOnlyMessageHandler;
import org.mvnsearch.spring.boot.nats.NatsContextAware;
import org.mvnsearch.spring.boot.nats.NatsDisposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AppInstanceOnlyMessageHandlerBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware, DisposableBean, NatsDisposable, NatsContextAware {
  private static final Logger logger = LoggerFactory.getLogger(AppInstanceOnlyMessageHandlerBeanPostProcessor.class);

  @Autowired
  @Lazy
  private Connection nc;
  private String instanceSubjectName;
  private final List<Dispatcher> dispatchers = new ArrayList<>();

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    final Environment env = applicationContext.getEnvironment();
    String connectionName = env.getProperty("nats.spring.connection-name", env.getProperty("spring.application.name", "unknown"));
    if (connectionName.length() > 36 && connectionName.substring(connectionName.length() - 36).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
      instanceSubjectName = connectionName;
    } else {
      instanceSubjectName = connectionName + "-" + UUID.randomUUID();
    }
    logger.info("NATS-020001: application instance subject name: {}", instanceSubjectName);
  }

  @Override
  public String getSubjectNameForAppInstance() {
    return this.instanceSubjectName;
  }

  @Override
  public void destroy() {
    for (Dispatcher dispatcher : dispatchers) {
      try {
        dispatcher.unsubscribe(instanceSubjectName);
        nc.closeDispatcher(dispatcher);
      } catch (Exception e) {
        logger.error("NATS-020500: Failed to close {}", instanceSubjectName, e);
      }
    }
  }


  @Override
  public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
    if (bean instanceof AppInstanceOnlyMessageHandler) {
      AppInstanceOnlyMessageHandler subscriber = (AppInstanceOnlyMessageHandler) bean;
      try {
        MessageHandler messageHandler = subscriber::onMessage;
        Dispatcher dispatcher = nc.createDispatcher(messageHandler);
        dispatcher.subscribe(instanceSubjectName);
        dispatchers.add(dispatcher);
      } catch (Exception e) {
        logger.error("NATS-020500: failed to process NatsInstanceSubjectSubscriber", e);
      }
    }
    return bean;
  }

}
