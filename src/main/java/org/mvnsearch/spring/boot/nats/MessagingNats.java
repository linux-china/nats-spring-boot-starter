package org.mvnsearch.spring.boot.nats;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.service.ServiceMessage;
import org.mvnsearch.spring.boot.nats.services.NatsServiceReturnValueHandler;
import org.mvnsearch.spring.boot.nats.services.NatsStrategies;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.ReactiveMessageHandler;
import org.springframework.messaging.handler.DestinationPatternsMessageCondition;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.MimeType;
import org.springframework.util.RouteMatcher;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;


public class MessagingNats {
  private final MimeType dataMimeType;
  private final Connection nc;
  private final ReactiveMessageHandler messageHandler;
  private final RouteMatcher routeMatcher;
  private final NatsStrategies strategies;

  MessagingNats(MimeType dataMimeType, Connection nc, ReactiveMessageHandler messageHandler, NatsStrategies strategies) {
    this.dataMimeType = dataMimeType;
    this.nc = nc;
    this.messageHandler = messageHandler;
    this.routeMatcher = strategies.routeMatcher();
    this.strategies = strategies;
  }

  /**
   * publish a message
   *
   * @param message message
   * @return void
   */
  public Mono<Void> publish(Message message) {
    return Mono.fromRunnable(() -> {
      try {
        nc.publish(message.getSubject(), message.getData());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  /**
   * receive a service message: request-reply
   *
   * @param serviceMessage service message
   * @return void
   */
  public Mono<Void> service(ServiceMessage serviceMessage) {
    AtomicReference<Mono<Message>> responseRef = new AtomicReference<>();
    MessageHeaders headers = createHeaders(serviceMessage, responseRef);
    org.springframework.messaging.Message<?> message = MessageBuilder.createMessage(serviceMessage.getData(), headers);
    return Mono.defer(() -> this.messageHandler.handleMessage(message))
      .then(Mono.defer(() -> responseRef.get() != null ? responseRef.get() : Mono.error(new IllegalStateException("Expected response"))))
      .flatMap(this::publish);
  }

  private MessageHeaders createHeaders(ServiceMessage serviceMessage, AtomicReference<Mono<Message>> responseRef) {
    MessageHeaderAccessor headers = new MessageHeaderAccessor();

    headers.setContentType(new MimeType("application", "json", StandardCharsets.UTF_8));
    headers.setHeader("subject", serviceMessage.getSubject());
    headers.setHeader("reply-to", serviceMessage.getReplyTo());
    serviceMessage.getHeaders().forEach(headers::setHeader);
    RouteMatcher.Route route = this.routeMatcher.parseRoute(serviceMessage.getSubject());
    headers.setHeader(DestinationPatternsMessageCondition.LOOKUP_DESTINATION_HEADER, route);
    headers.setLeaveMutable(true);
    if (responseRef != null) {
      headers.setHeader(NatsServiceReturnValueHandler.RESPONSE_HEADER, responseRef);
    }
    return headers.getMessageHeaders();
  }

}
