package org.mvnsearch.spring.boot.nats.configuration;

import io.nats.client.Connection;
import io.nats.client.impl.Headers;
import io.nats.service.Group;
import io.nats.service.Service;
import io.nats.service.ServiceBuilder;
import io.nats.service.ServiceEndpoint;
import org.mvnsearch.spring.boot.nats.MessagingNats;
import org.mvnsearch.spring.boot.nats.NatsContextAware;
import org.mvnsearch.spring.boot.nats.annotation.NatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.messaging.handler.annotation.MessageMapping;

import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

public class NatsServiceBeanPostProcessor implements BeanPostProcessor, DisposableBean {
  private static final Logger logger = LoggerFactory.getLogger(NatsServiceBeanPostProcessor.class);
  private static final byte[] EMPTY_BYTES = new byte[]{};
  @Autowired
  @Lazy
  private Connection nc;
  @Autowired
  @Lazy
  private MessagingNats messagingNats;
  @Autowired
  @Lazy
  private NatsContextAware natsContextAware;
  @Autowired
  @Lazy
  private Environment env;
  private final List<Service> natsServices = new ArrayList<>();

  public List<Service> getNatsServices() {
    return natsServices;
  }

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
        metadata.put("appInstanceSubject", natsContextAware.getSubjectNameForAppInstance());
        metadata.put("appName", env.getProperty("spring.application.name", "unknown-app"));
        metadata.put("serverPort", env.getProperty("server.port", "0"));
        metadata.put("managementServerPort", env.getProperty("management.server.port", "0"));
        metadata.put("serviceInterface", clazz.getCanonicalName());
        try {
          String serverIp = getServerIp();
          if (serverIp != null) {
            metadata.put("serverIp", serverIp);
          }
        } catch (Exception ignore) {

        }
        // construct service endpoint
        ServiceEndpoint serviceEndpoint = ServiceEndpoint.builder()
          .endpointName(requestMapping4Method.value()[0])
          .group(serviceGroup)
          .endpointMetadata(metadata)
          .handler(msg -> {
            try {
              messagingNats.service(msg).doOnError(e -> {
                  String fullName = clazz.getCanonicalName() + "." + method.getName();
                  logger.error("NATS-001500: failed to call NATS service: {}", fullName, e);
                  Headers headers = new Headers();
                  headers.put("error", "NATS-001500: failed to call NATS service: " + fullName);
                  msg.respond(nc, EMPTY_BYTES, headers);
                })
                .subscribe();
            } catch (Exception e) {
              String fullName = clazz.getCanonicalName() + "." + method.getName();
              logger.error("NATS-001500: failed to call NATS service: {}", fullName, e);
              Headers headers = new Headers();
              headers.put("error", "NATS-001500: failed to call NATS service: " + fullName);
              msg.respond(nc, EMPTY_BYTES, headers);
            }
          })
          .build();
        // add service endpoint
        serviceBuilder.addServiceEndpoint(serviceEndpoint);
      }
    }
    Service service = serviceBuilder.connection(nc).build();
    service.startService();
    return service;
  }


  public String getServerIp() throws Exception {
    String serverIp = null;
    Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
    while (networkInterfaces.hasMoreElements()) {
      NetworkInterface networkInterface = networkInterfaces.nextElement();
      Enumeration<?> ee = networkInterface.getInetAddresses();
      while (ee.hasMoreElements()) {
        InetAddress inetAddress = (InetAddress) ee.nextElement();
        if (!inetAddress.isLoopbackAddress()) {
          if (inetAddress instanceof Inet4Address) {
            serverIp = inetAddress.getHostAddress();
            break;
          }
        }
      }
    }
    if (serverIp == null) {
      List<String> dnsServers = Arrays.asList("1.1.1.1", "8.8.8.8");
      for (String dnsServer : dnsServers) {
        try (final DatagramSocket socket = new DatagramSocket()) {
          socket.connect(InetAddress.getByName(dnsServer), 10002);
          serverIp = socket.getLocalAddress().getHostAddress();
          break;
        }
      }
    }
    return serverIp;
  }

}
