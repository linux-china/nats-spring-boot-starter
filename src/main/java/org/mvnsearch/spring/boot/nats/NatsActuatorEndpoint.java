package org.mvnsearch.spring.boot.nats;

import io.nats.service.InfoResponse;
import io.nats.service.Service;
import io.nats.spring.boot.autoconfigure.NatsProperties;
import org.mvnsearch.spring.boot.nats.configuration.NatsServiceBeanPostProcessor;
import org.springframework.beans.BeansException;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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

  public NatsActuatorEndpoint(NatsProperties natsProperties) {
  }

  @ReadOperation
  public Map<String, Object> info() {
    Map<String, Object> info = new HashMap<>();
    List<Map<String, Object>> services = new ArrayList<>();
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
    info.put("services", services);
    return info;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}
