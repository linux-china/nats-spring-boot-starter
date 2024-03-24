package org.mvnsearch.spring.boot.nats.client;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import org.mvnsearch.spring.boot.nats.annotation.MessagingExchange;
import org.mvnsearch.spring.boot.nats.annotation.NatsExchange;
import org.mvnsearch.spring.boot.nats.annotation.ServiceExchange;
import org.mvnsearch.spring.boot.nats.serialization.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class NatsServiceInvocationHandler implements InvocationHandler {
  private static final Logger logger = LoggerFactory.getLogger(NatsServiceInvocationHandler.class);

  private final Connection nc;
  private final Class<?> serviceInterface;
  private String contentType = "tex/plain";
  private String topicPath = null;
  private final Map<Method, Class<?>> methodReturnTypeMap = new ConcurrentHashMap<>();

  public NatsServiceInvocationHandler(Connection nc, Class<?> serviceInterface) {
    this.nc = nc;
    this.serviceInterface = serviceInterface;
    final NatsExchange natsExchange = serviceInterface.getAnnotation(NatsExchange.class);
    if (natsExchange != null) {
      this.contentType = natsExchange.contentType();
      if (!natsExchange.path().isEmpty()) {
        topicPath = natsExchange.path();
      }
    }
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    //interface default method validation for JDK Proxy only, not necessary for ByteBuddy
    if (method.isDefault()) {
      return DefaultMethodHandler.getMethodHandle(method, serviceInterface).bindTo(proxy).invokeWithArguments(args);
    } else if (method.getDeclaringClass().equals(Object.class)) { //delegate hashCode, equals, or toString methods to this
      return method.invoke(this);
    }
    // proxy for interface methods
    byte[] paramBytes = SerializationUtil.toBytes(args[0], contentType);
    if (!methodReturnTypeMap.containsKey(method)) {
      methodReturnTypeMap.put(method, parseInferredClass(method.getGenericReturnType()));
    }
    Class<?> returnType = methodReturnTypeMap.get(method);
    ServiceExchange serviceExchange = AnnotationUtils.findAnnotation(method, ServiceExchange.class);
    MessagingExchange messagingExchange = AnnotationUtils.findAnnotation(method, MessagingExchange.class);
    if (serviceExchange != null) {
      String endpoint = serviceExchange.value();
      if (topicPath != null && !topicPath.isEmpty()) {
        endpoint = topicPath + "." + endpoint;
      }
      CompletableFuture<Message> result = nc.request(endpoint, paramBytes);
      return Mono.fromFuture(result).handle((msg, sink) -> {
        byte[] bytes = msg.getData();
        final Headers headers = msg.getHeaders();
        if (headers != null && headers.containsKey("error")) {
          sink.error(new Exception(headers.getFirst("error")));
        } else if (bytes == null || bytes.length == 0) { // return value(Mono) is empty
          sink.complete();
        } else {
          try {
            sink.next(SerializationUtil.convert(bytes, returnType, contentType));
          } catch (Exception e) {
            logger.error("NATS-020500: failed to convert bytes to object {}", returnType.getCanonicalName(), e);
            sink.error(e);
          }
        }
      });
    } else if (messagingExchange != null) { // publish
      String endpoint = messagingExchange.value();
      if (topicPath != null && !topicPath.isEmpty()) {
        endpoint = topicPath + "." + endpoint;
      }
      nc.publish(endpoint, paramBytes);
      final Class<?> methodReturnType = method.getReturnType();
      return methodReturnType == Mono.class ? Mono.empty() : null;
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
            logger.error("NATS-100500: failed to resolve inferred class: {}", genericType.getTypeName(), e);
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
