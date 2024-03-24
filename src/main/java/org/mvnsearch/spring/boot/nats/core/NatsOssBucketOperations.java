package org.mvnsearch.spring.boot.nats.core;

import io.nats.client.api.ObjectInfo;
import io.nats.client.api.ObjectMeta;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Nats OSS Bucket operations
 *
 * @author linux_china
 */
public interface NatsOssBucketOperations {

  @Nullable
  ObjectInfo getInfo(String objectName) throws IOException;

  ObjectInfo put(String objectName, InputStream inputStream) throws IOException;

  @Nullable
  ObjectInfo get(String objectName, OutputStream outputStream) throws IOException;

  @Nullable
  ObjectInfo updateMeta(String objectName, ObjectMeta meta) throws IOException;

  @Nullable
  ObjectInfo delete(String objectName) throws IOException;

  Flux<ObjectInfo> watch();
}
