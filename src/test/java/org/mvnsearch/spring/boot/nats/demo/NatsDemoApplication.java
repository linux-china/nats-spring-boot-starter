package org.mvnsearch.spring.boot.nats.demo;

import io.nats.client.Message;
import org.mvnsearch.spring.boot.nats.AppInstanceOnlyMessageHandler;
import org.mvnsearch.spring.boot.nats.annotation.NatsSubscriber;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * NATS demo application
 *
 * @author linux_china
 */
@SpringBootApplication
public class NatsDemoApplication implements AppInstanceOnlyMessageHandler {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(NatsDemoApplication.class, args);
        System.out.println("app:" + run.getId());
    }

    @NatsSubscriber(subject = "subject1")
    public void handler(Message msg) {
        System.out.println(msg.getSubject());
    }

    @Override
    public void onMessage(Message msg) throws InterruptedException {
        System.out.println("Received message from:" + msg.getSubject());
    }
}
