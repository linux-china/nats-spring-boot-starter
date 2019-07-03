package org.mvnsearch.spring.boot.nats.streaming;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;

import io.nats.streaming.Message;
import io.nats.streaming.MessageHandler;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.Subscription;
import io.nats.streaming.SubscriptionOptions;

/**
 * NatsStreamingSubscriber annotation bean post processor: register subscriber on NATS Streaming connection
 *
 * @author wisni
 */
public class NatsStreamingSubscriberAnnotationBeanPostProcessor
    implements BeanPostProcessor, Ordered, BeanFactoryAware, InitializingBean, DisposableBean {

    private Logger log = LoggerFactory.getLogger(NatsStreamingSubscriberAnnotationBeanPostProcessor.class);
    private BeanFactory beanFactory;
    private Map<NatsStreamingSubscriber, Subscription> subscriptions = new HashMap<>();
    private StreamingConnection natsStreaming;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.natsStreaming = this.beanFactory.getBean(StreamingConnection.class);
    }

    public Map<NatsStreamingSubscriber, Subscription> getSubscriptions() {
        return this.subscriptions;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        Map<Method, Set<NatsStreamingSubscriber>> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
            (MethodIntrospector.MetadataLookup<Set<NatsStreamingSubscriber>>) method -> {
                Set<NatsStreamingSubscriber> listenerMethods = this.findSubscriberAnnotations(method);
                return (!listenerMethods.isEmpty() ? listenerMethods : null);
            });

        // Non-empty set of subscribed methods
        if (!annotatedMethods.isEmpty()) {
            try {
                for (Map.Entry<Method, Set<NatsStreamingSubscriber>> entry : annotatedMethods.entrySet()) {
                    Method method = entry.getKey();
                    for (NatsStreamingSubscriber listener : entry.getValue()) {
                        this.processNatsStreamingSubscriber(listener, method, bean, beanName);
                    }
                }
            } catch (Exception e) {
                throw new BeanCreationException("Failed to construct NATS Streaming Subscriber handler", e);
            }
        }

        return bean;
    }

    /*
     * AnnotationUtils.getRepeatableAnnotations does not look at interfaces
     */
    private Set<NatsStreamingSubscriber> findSubscriberAnnotations(Method method) {
        Set<NatsStreamingSubscriber> subscribers = new HashSet<>();
        NatsStreamingSubscriber natsStreamingSubscriber = AnnotationUtils.findAnnotation(method, NatsStreamingSubscriber.class);
        if (natsStreamingSubscriber != null) {
            subscribers.add(natsStreamingSubscriber);
        }
        NatsStreamingSubscribers natsStreamingSubscribers = AnnotationUtils.findAnnotation(method, NatsStreamingSubscribers.class);
        if (natsStreamingSubscribers != null) {
            subscribers.addAll(Arrays.asList(natsStreamingSubscribers.value()));
        }

        return subscribers;
    }

    private void processNatsStreamingSubscriber(NatsStreamingSubscriber natsStreamingSubscriber, Method method, Object bean, String beanName)
        throws NoSuchMethodException, IllegalAccessException, IOException, InterruptedException, TimeoutException {
        MethodHandle methodHandler = this.getMethodHandler(method);
        MessageHandler messageHandler = msg -> {
            try {
                methodHandler.invoke(bean, msg);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };

        Subscription subscription;
        if ("".equals(natsStreamingSubscriber.queueGroup())) {
            subscription = this.natsStreaming.subscribe(natsStreamingSubscriber.subject(), messageHandler,
                this.getSubscriptionOptions(natsStreamingSubscriber));
        } else {
            subscription = this.natsStreaming.subscribe(natsStreamingSubscriber.subject(), natsStreamingSubscriber.queueGroup(), messageHandler,
                this.getSubscriptionOptions(natsStreamingSubscriber));
        }
        this.subscriptions.put(natsStreamingSubscriber, subscription);
    }

    private SubscriptionOptions getSubscriptionOptions(NatsStreamingSubscriber natsStreamingSubscriber) {
        SubscriptionOptions.Builder optionsBuilder = new SubscriptionOptions.Builder();
        if (!"".equals(natsStreamingSubscriber.durableName())) {
            optionsBuilder.durableName(natsStreamingSubscriber.durableName());
        }

        if (natsStreamingSubscriber.manualAcks()) {
            optionsBuilder.manualAcks();
        }

        if (!"".equals(natsStreamingSubscriber.ackWait())) {
            optionsBuilder.ackWait(Duration.ofSeconds(Long.parseLong(natsStreamingSubscriber.ackWait())));
        }

        if (!"".equals(natsStreamingSubscriber.maxInFlight())) {
            optionsBuilder.maxInFlight(Integer.valueOf(natsStreamingSubscriber.maxInFlight()));
        }

        return optionsBuilder.build();
    }

    @Override
    public void destroy() throws Exception {
        for (Map.Entry<NatsStreamingSubscriber, Subscription> entry : this.subscriptions.entrySet()) {
            Subscription subscription = entry.getValue();
            NatsStreamingSubscriber natsStreamingSubscriber = entry.getKey();
            try {
                subscription.close();
            } catch (Exception e) {
                this.log.error("Failed to close " + natsStreamingSubscriber.subject(), e);
            }
        }
    }

    private MethodHandle getMethodHandler(Method method) throws NoSuchMethodException, IllegalAccessException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType mt = MethodType.methodType(void.class, Message.class);

        return lookup.findVirtual(method.getDeclaringClass(), method.getName(), mt);
    }
}
