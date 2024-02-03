package org.mvnsearch.spring.boot.nats.configuration;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import org.mvnsearch.spring.boot.nats.NatsReactive;
import org.mvnsearch.spring.boot.nats.annotation.NatsSubscriber;
import org.mvnsearch.spring.boot.nats.annotation.NatsSubscribers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * NatsSubscriber annotation bean post processor: register subscriber on NATS connection
 *
 * @author linux_china
 */
public class NatsSubscriberAnnotationBeanPostProcessor implements NatsReactive, BeanPostProcessor, Ordered, BeanFactoryAware, InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(NatsSubscriberAnnotationBeanPostProcessor.class);
    private BeanFactory beanFactory;
    private final Map<NatsSubscriber, Dispatcher> subscriptions = new HashMap<>();
    private Connection nc;

    @Override
    public void afterPropertiesSet() throws Exception {
        nc = beanFactory.getBean(Connection.class);
    }

    public Map<NatsSubscriber, Dispatcher> getSubscriptions() {
        return subscriptions;
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
        Map<Method, Set<NatsSubscriber>> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
                (MethodIntrospector.MetadataLookup<Set<NatsSubscriber>>) method -> {
                    Set<NatsSubscriber> listenerMethods = findSubscriberAnnotations(method);
                    return (!listenerMethods.isEmpty() ? listenerMethods : null);
                });
        // Non-empty set of subscribed methods
        if (!annotatedMethods.isEmpty()) {
            try {
                for (Map.Entry<Method, Set<NatsSubscriber>> entry : annotatedMethods.entrySet()) {
                    Method method = entry.getKey();
                    for (NatsSubscriber listener : entry.getValue()) {
                        processNatsSubscriber(listener, method, bean, beanName);
                    }
                }
            } catch (Exception e) {
                throw new BeanCreationException("Failed to construct NATS Subscriber handler", e);
            }
        }
        return bean;
    }


    /*
     * AnnotationUtils.getRepeatableAnnotations does not look at interfaces
     */
    private Set<NatsSubscriber> findSubscriberAnnotations(Method method) {
        Set<NatsSubscriber> subscribers = new HashSet<>();
        NatsSubscriber natsSubscriber = AnnotationUtils.findAnnotation(method, NatsSubscriber.class);
        if (natsSubscriber != null) {
            subscribers.add(natsSubscriber);
        }
        NatsSubscribers natsSubscribers = AnnotationUtils.findAnnotation(method, NatsSubscribers.class);
        if (natsSubscribers != null) {
            subscribers.addAll(Arrays.asList(natsSubscribers.value()));
        }
        return subscribers;
    }

    private void processNatsSubscriber(NatsSubscriber natsSubscriber, Method method, Object bean, String beanName) throws NoSuchMethodException, IllegalAccessException {
        //use method handler instead of reflection for performance
        MethodHandle methodHandler = getMethodHandler(method);
        MessageHandler messageHandler = msg -> {
            try {
                methodHandler.invoke(bean, msg);
                // method.invoke(bean, msg);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
        Dispatcher dispatcher;
        if ("".equals(natsSubscriber.queueGroup())) {
            dispatcher = nc.createDispatcher(messageHandler);
            dispatcher.subscribe(natsSubscriber.subject());
        } else {
            dispatcher = nc.createDispatcher(messageHandler);
            dispatcher.subscribe(natsSubscriber.subject(), natsSubscriber.queueGroup());
        }
        subscriptions.put(natsSubscriber, dispatcher);
    }

    @Override
    public Mono<Void> publish(String subject, byte[] body) {
        CompletableFuture<Message> request = nc.request(subject, body);
        return Mono.fromFuture(request).then();
    }

    @Override
    public Flux<byte[]> subscribe(String subject) {
        return Flux.create(sink -> {
            Dispatcher dispatcher = nc.createDispatcher(message -> {
                sink.next(message.getData());
            });
            subscriptions.put(createAnnotation(subject, null), dispatcher);
        });
    }

    @Override
    public void destroy() throws Exception {
        for (Map.Entry<NatsSubscriber, Dispatcher> entry : subscriptions.entrySet()) {
            Dispatcher dispatcher = entry.getValue();
            NatsSubscriber natsSubscriber = entry.getKey();
            try {
                dispatcher.unsubscribe(natsSubscriber.subject());
                nc.closeDispatcher(dispatcher);
            } catch (Exception e) {
                log.error("Failed to close " + natsSubscriber.subject(), e);
            }
        }

    }

    private MethodHandle getMethodHandler(Method method) throws NoSuchMethodException, IllegalAccessException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType mt = MethodType.methodType(void.class, Message.class);
        return lookup.findVirtual(method.getDeclaringClass(), method.getName(), mt);
    }

    public NatsSubscriber createAnnotation(String subject, String queueGroup) {
        return new NatsSubscriber() {
            @Override
            public String subject() {
                return subject;
            }

            @Override
            public String queueGroup() {
                return queueGroup;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return NatsSubscriber.class;
            }
        };
    }
}
