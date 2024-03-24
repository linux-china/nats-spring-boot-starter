package org.mvnsearch.spring.boot.nats.actuator;

import io.nats.client.Dispatcher;
import io.nats.service.InfoResponse;
import io.nats.service.Service;
import io.nats.spring.boot.autoconfigure.NatsProperties;
import org.mvnsearch.spring.boot.nats.annotation.NatsSubscriber;
import org.mvnsearch.spring.boot.nats.configuration.NatsServiceBeanPostProcessor;
import org.mvnsearch.spring.boot.nats.configuration.NatsSubscriberAnnotationBeanPostProcessor;
import org.springframework.beans.BeansException;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NATS actuator endpoint
 *
 * @author linux_china
 */
@Endpoint(id = "nats")
public class NatsActuatorEndpoint implements ApplicationContextAware {
  private ApplicationContext applicationContext;
  private final NatsProperties natsProperties;

  public NatsActuatorEndpoint(NatsProperties natsProperties) {
    this.natsProperties = natsProperties;
  }

  @ReadOperation
  public Map<String, Object> info() {
    Map<String, Object> info = new HashMap<>();
    List<Map<String, Object>> services = new ArrayList<>();
    // subscribers
    final Map<NatsSubscriber, Dispatcher> subscriptions = applicationContext.getBean(NatsSubscriberAnnotationBeanPostProcessor.class).getSubscriptions();
    List<Map<String, Object>> natsSubscriptions = new ArrayList<>();
    for (NatsSubscriber natsSubscriber : subscriptions.keySet()) {
      Map<String, Object> subscriberInfo = new HashMap<>();
      subscriberInfo.put("subject", natsSubscriber.subject());
      if (!natsSubscriber.queueGroup().isEmpty()) {
        subscriberInfo.put("queueGroup", natsSubscriber.queueGroup());
      }
      natsSubscriptions.add(subscriberInfo);
    }
    info.put("subscribers", natsSubscriptions);
    // services
    final List<Service> natsServices = applicationContext.getBean(NatsServiceBeanPostProcessor.class).getNatsServices();
    for (Service natsService : natsServices) {
      Map<String, Object> serviceInfo = new HashMap<>();
      final InfoResponse infoResponse = natsService.getInfoResponse();
      serviceInfo.put("id", natsService.getId());
      serviceInfo.put("name", natsService.getName());
      serviceInfo.put("version", natsService.getVersion());
      serviceInfo.put("description", natsService.getDescription());
      List<Map<String, Object>> endpoints = new ArrayList<>();
      for (io.nats.service.Endpoint endpoint : infoResponse.getEndpoints()) {
        Map<String, Object> endpointInfo = new HashMap<>();
        endpointInfo.put("name", endpoint.getName());
        endpointInfo.put("subject", endpoint.getSubject());
        endpointInfo.put("group", endpoint.getQueueGroup());
        endpoints.add(endpointInfo);
      }
      serviceInfo.put("endpoints", endpoints);
      services.add(serviceInfo);
    }
    if (!services.isEmpty()) {
      info.put("services", services);
    }
    return info;
  }

  @Override
  public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}
