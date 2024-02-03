package org.mvnsearch.spring.boot.nats.configuration;


import io.nats.client.Connection;
import io.nats.client.api.KeyValueEntry;
import io.nats.client.api.KeyValueWatcher;
import io.nats.client.impl.NatsKeyValueWatchSubscription;
import org.mvnsearch.spring.boot.nats.annotation.NatsDurableComponent;
import org.mvnsearch.spring.boot.nats.annotation.NatsKeyWatcher;
import org.mvnsearch.spring.boot.nats.serialization.JsonSerialization;
import org.mvnsearch.spring.boot.nats.serialization.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class NatsDurableBeanPostProcessor implements BeanPostProcessor, DisposableBean {
  private static final Logger logger = LoggerFactory.getLogger(NatsDurableBeanPostProcessor.class);

  @Autowired
  @Lazy
  private Connection nc;
  private final List<NatsKeyValueWatchSubscription> subscriptions = new ArrayList<>();

  @Override
  public void destroy() throws Exception {
    for (NatsKeyValueWatchSubscription subscription : subscriptions) {
      try {
        subscription.close();
      } catch (Exception ignore) {

      }
    }
  }


  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    Class<?> clazz = bean.getClass();
    NatsDurableComponent natsDurableComponent = AnnotationUtils.findAnnotation(clazz, NatsDurableComponent.class);
    if (natsDurableComponent != null) {
      for (Method method : clazz.getDeclaredMethods()) {
        NatsKeyWatcher watcher = AnnotationUtils.findAnnotation(method, NatsKeyWatcher.class);
        if (watcher != null) {
          try {
            watchKey(nc, watcher, bean, method);
          } catch (Exception ignore) {

          }
        }
      }

    }
    return bean;
  }

  public void watchKey(Connection nc, NatsKeyWatcher natsKeyWatcher, Object bean, Method method) throws Exception {
    NatsKeyValueWatchSubscription subscription = nc.keyValue(natsKeyWatcher.bucket()).watch(natsKeyWatcher.key(), new KeyValueWatcher() {
      @Override
      public void watch(KeyValueEntry keyValueEntry) {
        try {
          byte[] textValue = keyValueEntry.getValue();
          Class<?> paramType = method.getParameterTypes()[0];
          Object param = SerializationUtil.convert(textValue, paramType);
          ReflectionUtils.invokeMethod(method, bean, param);
        } catch (Exception e) {
          logger.error("NATS-010500: failed to update watched key", e);
        }
      }

      @Override
      public void endOfData() {

      }
    });
    this.subscriptions.add(subscription);
  }

}
