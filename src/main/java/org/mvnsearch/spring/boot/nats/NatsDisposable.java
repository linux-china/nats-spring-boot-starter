package org.mvnsearch.spring.boot.nats;

/**
 * Nats disposable
 *
 * @author linux_china
 */
@FunctionalInterface
public interface NatsDisposable {
  void destroy() throws java.lang.Exception;
}
