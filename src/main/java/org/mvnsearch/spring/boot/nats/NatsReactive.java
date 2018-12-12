package org.mvnsearch.spring.boot.nats;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * NATS interface for subscribe with Reactive support
 *
 * @author linux_china
 */
public interface NatsReactive {

    Mono<Void> publish(String subject, byte[] body);

    Flux<byte[]> subscribe(String subject);
}
