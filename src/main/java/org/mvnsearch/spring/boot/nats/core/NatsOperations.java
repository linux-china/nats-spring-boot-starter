package org.mvnsearch.spring.boot.nats.core;

import io.nats.client.Message;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;

/**
 * Nats Operations
 *
 * @author linux_china
 */
public interface NatsOperations {

  void publish(@NonNull Message message);

  void publish(@NonNull String subject, byte[] body);

  void publish(@NonNull String subject, @NonNull Map<String, String> headers, byte[] body);

  void requestReply(@NonNull String subject, @NonNull String replyTo, byte[] body);

  Flux<Message> subscribe(@NonNull String subject);

  Flux<Message> subscribe(@NonNull String subject, String queueGroup);

  NatsKeyValueOperations boundKvOps(@NonNull String bucket) throws IOException;

  NatsOssBucketOperations boundOssOps(@NonNull String bucket) throws IOException;

}
