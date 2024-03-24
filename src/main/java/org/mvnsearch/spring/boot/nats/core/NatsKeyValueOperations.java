package org.mvnsearch.spring.boot.nats.core;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.IOException;

/**
 * Nats KeyValue operations
 *
 * @author linux_china
 */
public interface NatsKeyValueOperations {
  @Nullable
  String get(String key) throws IOException;

  void put(@NonNull String key, @NonNull String value) throws IOException;

  void delete(@NonNull String key) throws IOException;
}
