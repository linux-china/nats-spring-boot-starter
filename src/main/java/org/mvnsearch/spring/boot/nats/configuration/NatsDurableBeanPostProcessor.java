package org.mvnsearch.spring.boot.nats.configuration;


import io.nats.client.Connection;
import io.nats.client.api.KeyValueEntry;
import io.nats.client.api.KeyValueWatcher;
import io.nats.client.impl.NatsKeyValueWatchSubscription;
import org.mvnsearch.spring.boot.nats.NatsDisposable;
import org.mvnsearch.spring.boot.nats.annotation.NatsDurableComponent;
import org.mvnsearch.spring.boot.nats.annotation.NatsKeyWatcher;
import org.mvnsearch.spring.boot.nats.serialization.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class NatsDurableBeanPostProcessor implements BeanFactoryAware, BeanPostProcessor, DisposableBean, NatsDisposable {
    private static final Logger logger = LoggerFactory.getLogger(NatsDurableBeanPostProcessor.class);
    private BeanFactory beanFactory;
    private EmbeddedValueResolver resolver;
    @Autowired
    @Lazy
    private Connection nc;
    private final List<NatsKeyValueWatchSubscription> subscriptions = new ArrayList<>();

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        this.resolver = new EmbeddedValueResolver((ConfigurableBeanFactory) beanFactory);
    }

    @Override
    public void destroy() {
        for (NatsKeyValueWatchSubscription subscription : subscriptions) {
            try {
                subscription.close();
            } catch (Exception ignore) {

            }
        }
    }


    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
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
        String bucket = natsKeyWatcher.bucket();
        String key = natsKeyWatcher.key();
        if (bucket.startsWith("${")) {
            bucket = resolver.resolveStringValue(bucket);
        }
        if (key.startsWith("${")) {
            key = resolver.resolveStringValue(key);
        }
        NatsKeyValueWatchSubscription subscription = nc.keyValue(bucket).watch(key, new KeyValueWatcher() {
            @Override
            public void watch(KeyValueEntry keyValueEntry) {
                try {
                    byte[] textValue = keyValueEntry.getValue();
                    Class<?> paramType = method.getParameterTypes()[0];
                    Object param = SerializationUtil.convert(textValue, paramType, "text/plain");
                    ReflectionUtils.invokeMethod(method, bean, param);
                } catch (Exception e) {
                    logger.error("NATS-020500: failed to update watched key", e);
                }
            }

            @Override
            public void endOfData() {

            }
        });
        this.subscriptions.add(subscription);
    }

}
