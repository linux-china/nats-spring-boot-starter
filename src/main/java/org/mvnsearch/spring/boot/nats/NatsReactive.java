package org.mvnsearch.spring.boot.nats;

import reactor.core.publisher.Flux;

/**
 * NATS interface for subscribe with Reactive support
 *
 * @author linux_china
 */
public interface NatsReactive {

    Flux<byte[]> subscribe(String topic);
}
