package org.mvnsearch.spring.boot.nats.core;

import io.nats.client.*;
import io.nats.client.api.ObjectInfo;
import io.nats.client.api.ObjectMeta;
import io.nats.client.api.ObjectStoreWatcher;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsObjectStoreWatchSubscription;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Nats template
 *
 * @author linux_china
 */
public class NatsTemplate implements NatsOperations {
  private final Connection nc;

  public NatsTemplate(Connection nc) {
    this.nc = nc;
  }

  @Override
  public void publish(@NonNull Message message) {
    nc.publish(message);
  }

  @Override
  public void publish(@NonNull String subject, byte[] body) {
    nc.publish(subject, body);
  }

  @Override
  public void publish(@NonNull String subject, Map<String, String> headers, byte[] body) {
    Headers msgHeaders = new Headers();
    headers.forEach(msgHeaders::put);
    nc.publish(subject, msgHeaders, body);
  }

  @Override
  public void requestReply(@NonNull String subject, @NonNull String replyTo, byte[] body) {
    nc.publish(subject, replyTo, body);
  }

  @Override
  public Flux<Message> subscribe(@NonNull String subject) {
    return Flux.create(sink -> {
      Dispatcher dispatcher = nc.createDispatcher(sink::next);
      dispatcher.subscribe(subject);
      sink.onDispose(() -> {
        dispatcher.unsubscribe(subject);
      });
    });
  }

  @Override
  public Flux<Message> subscribe(@NonNull String subject, @NonNull String queueGroup) {
    return Flux.create(sink -> {
      Dispatcher dispatcher = nc.createDispatcher(sink::next);
      dispatcher.subscribe(subject, queueGroup);
      sink.onDispose(() -> {
        dispatcher.unsubscribe(subject);
      });
    });
  }

  @Override
  public NatsKeyValueOperations boundKvOps(@NonNull String bucket) throws IOException {
    final KeyValue keyValue = nc.keyValue(bucket);
    return new NatsKeyValueOperations() {
      @Override
      public String get(String key) throws IOException {
        try {
          return keyValue.get(key).getValueAsString();
        } catch (JetStreamApiException e) {
          throw new IOException(e);
        }
      }

      @Override
      public void put(@NonNull String key, @NonNull String value) throws IOException {
        try {
          keyValue.put(key, value);
        } catch (JetStreamApiException e) {
          throw new IOException(e);
        }
      }

      @Override
      public void delete(@NonNull String key) throws IOException {
        try {
          keyValue.delete(key);
        } catch (JetStreamApiException e) {
          throw new IOException(e);
        }
      }
    };
  }

  @Override
  public NatsOssBucketOperations boundOssOps(@NonNull String bucket) throws IOException {
    final ObjectStore objectStore = nc.objectStore(bucket);
    return new NatsOssBucketOperations() {
      @Override
      public ObjectInfo getInfo(String objectName) throws IOException {
        try {
          return objectStore.getInfo(objectName);
        } catch (JetStreamApiException e) {
          throw new IOException(e);
        }
      }

      @Override
      public ObjectInfo put(String objectName, InputStream inputStream) throws IOException {
        try {
          return objectStore.put(objectName, inputStream);
        } catch (Exception e) {
          throw new IOException(e);
        }
      }

      @Override
      public ObjectInfo get(String objectName, OutputStream outputStream) throws IOException {
        try {
          return objectStore.get(objectName, outputStream);
        } catch (Exception e) {
          throw new IOException(e);
        }

      }

      @Override
      public ObjectInfo updateMeta(String objectName, ObjectMeta meta) throws IOException {
        try {
          return objectStore.updateMeta(objectName, meta);
        } catch (JetStreamApiException e) {
          throw new IOException(e);
        }
      }

      @Override
      public ObjectInfo delete(String objectName) throws IOException {
        try {
          return objectStore.delete(objectName);
        } catch (JetStreamApiException e) {
          throw new IOException(e);
        }
      }

      @Override
      public Flux<ObjectInfo> watch() {
        return Flux.create(sink -> {
          try {
            @SuppressWarnings("resource") final NatsObjectStoreWatchSubscription subscription = objectStore.watch(new ObjectStoreWatcher() {
              @Override
              public void watch(ObjectInfo objectInfo) {
                sink.next(objectInfo);
              }

              @Override
              public void endOfData() {

              }
            });
            sink.onDispose(() -> {
              try {
                subscription.close();
              } catch (Exception ignore) {
              }
            });
          } catch (Exception e) {
            sink.error(e);
          }
        });
      }
    };
  }
}
