package org.mvnsearch.spring.boot.nats.configuration;


import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.MessageHandler;
import org.mvnsearch.spring.boot.nats.AppInstanceOnlyMessageHandler;
import org.mvnsearch.spring.boot.nats.NatsContextAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AppInstanceOnlyMessageHandlerBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware, DisposableBean, NatsContextAware {
    private static final Logger logger = LoggerFactory.getLogger(AppInstanceOnlyMessageHandlerBeanPostProcessor.class);

    @Autowired
    @Lazy
    private Connection nc;
    private String instanceSubjectName;
    private final List<Dispatcher> dispatchers = new ArrayList<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        String appName = applicationContext.getEnvironment().getProperty("spring.application.name", "unknown");
        instanceSubjectName = appName + "-" + UUID.randomUUID();
        logger.info("NATS-001001: application instance subject name: " + instanceSubjectName);
    }

    @Override
    public String getSubjectNameForAppInstance() {
        return this.instanceSubjectName;
    }

    @Override
    public void destroy() throws Exception {
        for (Dispatcher dispatcher : dispatchers) {
            try {
                dispatcher.unsubscribe(instanceSubjectName);
                nc.closeDispatcher(dispatcher);
            } catch (Exception e) {
                logger.error("Failed to close " + instanceSubjectName, e);
            }
        }
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AppInstanceOnlyMessageHandler) {
            AppInstanceOnlyMessageHandler subscriber = (AppInstanceOnlyMessageHandler) bean;
            try {
                MessageHandler messageHandler = subscriber::onMessage;
                Dispatcher dispatcher = nc.createDispatcher(messageHandler);
                dispatcher.subscribe(instanceSubjectName);
                dispatchers.add(dispatcher);
            } catch (Exception e) {
                logger.error("Failed to process NatsInstanceSubjectSubscriber", e);
            }
        }
        return bean;
    }

}
