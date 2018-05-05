package org.mvnsearch.spring.boot.nats;

import io.nats.client.AsyncSubscription;
import io.nats.client.Connection;
import io.nats.client.MessageHandler;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * NatsSubscriber annotation bean post processor: register subscriber on NATS connection
 *
 * @author linux_china
 */
public class NatsSubscriberAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered, BeanFactoryAware, DisposableBean {
    private BeanFactory beanFactory;
    private List<AsyncSubscription> subscriptions = new ArrayList<>();

    public List<AsyncSubscription> getSubscriptions() {
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
            for (Map.Entry<Method, Set<NatsSubscriber>> entry : annotatedMethods.entrySet()) {
                Method method = entry.getKey();
                for (NatsSubscriber listener : entry.getValue()) {
                    processNatsSubscriber(listener, method, bean, beanName);
                }
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

    private void processNatsSubscriber(NatsSubscriber natsSubscriber, Method method, Object bean, String beanName) {
        Connection nats = beanFactory.getBean(Connection.class);
        MessageHandler messageHandler = msg -> {
            try {
                method.invoke(bean, msg);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        AsyncSubscription subscription;
        natsSubscriber.queueGroup();
        if ("".equals(natsSubscriber.queueGroup())) {
            subscription = nats.subscribe(natsSubscriber.subject(), messageHandler);
        } else {
            subscription = nats.subscribe(natsSubscriber.subject(), natsSubscriber.queueGroup(), messageHandler);
        }
        subscriptions.add(subscription);
    }

    @Override
    public void destroy() throws Exception {
        for (AsyncSubscription subscription : subscriptions) {
            subscription.close();
        }
    }
}
