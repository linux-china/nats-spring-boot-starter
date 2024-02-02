package org.mvnsearch.spring.boot.nats.configuration;

import io.nats.client.Connection;
import io.nats.client.Message;
import org.mvnsearch.spring.boot.nats.annotation.MessagingExchange;
import org.mvnsearch.spring.boot.nats.annotation.ServiceExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class NatsServiceInvocationHandler implements InvocationHandler {
  private static final Logger logger = LoggerFactory.getLogger(NatsServiceInvocationHandler.class);

  private final Connection nc;
  private final Class<?> serviceInterface;
  private final Map<Method, Class<?>> methodReturnTypeMap = new ConcurrentHashMap<>();

  public NatsServiceInvocationHandler(Connection nc, Class<?> serviceInterface) {
    this.nc = nc;
    this.serviceInterface = serviceInterface;
  }

  @SuppressWarnings("SuspiciousInvocationHandlerImplementation")
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    byte[] paramBytes = JsonUtil.toJsonBytes(args[0]);
    if (!methodReturnTypeMap.containsKey(method)) {
      methodReturnTypeMap.put(method, parseInferredClass(method.getGenericReturnType()));
    }
    Class<?> returnType = methodReturnTypeMap.get(method);
    ServiceExchange serviceExchange = AnnotationUtils.findAnnotation(method, ServiceExchange.class);
    MessagingExchange messagingExchange = AnnotationUtils.findAnnotation(method, MessagingExchange.class);
    if (serviceExchange != null) {
      String endpoint = serviceExchange.value();
      CompletableFuture<Message> result = nc.request(endpoint, paramBytes);
      return Mono.fromFuture(result).map(msg -> {
        String textValue = new String(msg.getData(), StandardCharsets.UTF_8);
        try {
          return JsonUtil.convert(textValue, returnType);
        } catch (Exception e) {
          logger.error("NATS-020500: failed to convert text " + textValue + " to object " + returnType.getCanonicalName(), e);
        }
        return textValue;
      });
    } else if (messagingExchange != null) { // publish
      nc.publish(messagingExchange.value(), paramBytes);
    }
    throw new Exception("Failed to call: " + serviceInterface.getCanonicalName() + "." + method.getName());
  }

  /**
   * get inferred class for Generic Type, please refer http://tutorials.jenkov.com/java-reflection/generics.html
   *
   * @param genericType generic type
   * @return inferred class
   */
  public static Class<?> parseInferredClass(Type genericType) {
    Class<?> inferredClass = null;
    if (genericType instanceof ParameterizedType) {
      Type[] typeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
      if (typeArguments.length > 0) {
        final Type typeArgument = typeArguments[0];
        if (typeArgument instanceof ParameterizedType) {
          inferredClass = (Class<?>) ((ParameterizedType) typeArgument).getActualTypeArguments()[0];
        } else if (typeArgument instanceof Class) {
          inferredClass = (Class<?>) typeArgument;
        } else {
          String typeName = typeArgument.getTypeName();
          if (typeName.contains(" ")) {
            typeName = typeName.substring(typeName.lastIndexOf(" ") + 1);
          }
          if (typeName.contains("<")) {
            typeName = typeName.substring(0, typeName.indexOf("<"));
          }
          try {
            inferredClass = Class.forName(typeName);
          } catch (Exception e) {
            logger.error("NATS-100500: failed to resolve inferred class: " + genericType.getTypeName(), e);
          }
        }
      }
    }
    if (inferredClass == null && genericType instanceof Class) {
      inferredClass = (Class<?>) genericType;
    }
    return inferredClass;
  }
}
