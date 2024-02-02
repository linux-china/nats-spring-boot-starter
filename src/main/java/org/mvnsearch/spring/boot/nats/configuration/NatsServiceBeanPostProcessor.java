package org.mvnsearch.spring.boot.nats.configuration;

import io.nats.client.Connection;
import io.nats.service.*;
import org.mvnsearch.spring.boot.nats.annotation.NatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NatsServiceBeanPostProcessor implements BeanPostProcessor, DisposableBean {
  private static final Logger logger = LoggerFactory.getLogger(NatsServiceBeanPostProcessor.class);

  @Autowired
  @Lazy
  private Connection nc;
  private final List<Service> natsServices = new ArrayList<>();


  @Override
  public void destroy() throws Exception {
    for (Service natsService : natsServices) {
      try {
        natsService.stop(true);
      } catch (Exception ignore) {

      }
    }
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    Class<?> clazz = bean.getClass();
    NatsService natsService = AnnotationUtils.findAnnotation(clazz, NatsService.class);
    if (natsService != null) {
      Service service = registerNatsService(bean, clazz, natsService);
      natsServices.add(service);
    }
    return bean;
  }

  public Service registerNatsService(Object bean, Class<?> clazz, NatsService natsService) {
    // NATS Service builder
    ServiceBuilder serviceBuilder = Service.builder()
      .name(natsService.name())
      .version(natsService.version())
      .description(natsService.description().trim());
    // service group
    Group serviceGroup = null;
    MessageMapping requestMapping4Class = AnnotationUtils.findAnnotation(clazz, MessageMapping.class);
    if (requestMapping4Class != null) {
      serviceGroup = new Group(requestMapping4Class.value()[0]);
    }
    // scan handler
    for (Method method : clazz.getDeclaredMethods()) {
      MessageMapping requestMapping4Method = AnnotationUtils.findAnnotation(method, MessageMapping.class);
      if (requestMapping4Method != null) {
        // endpoint metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("ip", "192.168.1.2");
        // construct service endpoint
        ServiceEndpoint serviceEndpoint = ServiceEndpoint.builder()
          .endpointName(requestMapping4Method.value()[0])
          .group(serviceGroup)
          .endpointMetadata(metadata)
          .handler(msg -> {
            try {
              Object param = msg;
              Class<?> paramType = method.getParameterTypes()[0];
              if (paramType == String.class) {
                param = new String(msg.getData(), StandardCharsets.UTF_8);
              } else if (paramType != ServiceMessage.class) {
                String textBody = new String(msg.getData(), StandardCharsets.UTF_8);
                param = JsonUtil.convert(textBody, paramType);
              }
              Object result = ReflectionUtils.invokeMethod(method, bean, param);
              if (result instanceof String) {
                msg.respond(nc, (String) result);
              } else {
                msg.respond(nc, JsonUtil.toJson(result));
              }
            } catch (Exception e) {
              String fullName = clazz.getCanonicalName() + "." + method.getName();
              logger.error("NATS-001500: failed to call NATS service: " + fullName, e);
              msg.respond(nc, "error: " + e.getMessage());
            }
          })
          .build();
        // add service endpoint
        serviceBuilder.addServiceEndpoint(serviceEndpoint);
      }
    }
    Service service = serviceBuilder.connection(nc).build();
    service.startService().join();
    return service;
  }

}