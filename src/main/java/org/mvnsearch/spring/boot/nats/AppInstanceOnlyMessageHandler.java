package org.mvnsearch.spring.boot.nats;

import io.nats.client.Message;

/**
 * Application instance only message handler
 *
 * @author linux_china
 */
@FunctionalInterface
public interface AppInstanceOnlyMessageHandler {
    void onMessage(Message msg) throws InterruptedException;
}
