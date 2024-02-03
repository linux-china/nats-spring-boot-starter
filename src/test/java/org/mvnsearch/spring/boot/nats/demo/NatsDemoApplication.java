package org.mvnsearch.spring.boot.nats.demo;

import io.nats.client.Message;
import org.mvnsearch.spring.boot.nats.AppInstanceOnlyMessageHandler;
import org.mvnsearch.spring.boot.nats.annotation.NatsSubscriber;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * NATS demo application
 *
 * @author linux_china
 */
@SpringBootApplication
public class NatsDemoApplication implements AppInstanceOnlyMessageHandler {

    public static void main(String[] args) {
        SpringApplication.run(NatsDemoApplication.class, args);
    }

    @NatsSubscriber(subject = "subject1")
    public void handleSubject1(Message msg) {
        System.out.println(msg.getSubject());
    }

    @Override
    public void onMessage(Message msg) throws InterruptedException {
        System.out.println("Received message from:" + msg.getSubject());
    }
}
