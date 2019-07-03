package org.mvnsearch.spring.boot.nats.demo;

import org.mvnsearch.spring.boot.nats.NatsSubscriber;
import org.mvnsearch.spring.boot.nats.streaming.NatsStreamingSubscriber;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.nats.client.Message;

/**
 * NATS demo application
 *
 * @author linux_china
 */
@SpringBootApplication
public class NatsDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(NatsDemoApplication.class, args);
    }

    @NatsSubscriber(subject = "topic.a")
    public void handler(Message msg) {
        System.out.println(msg.getSubject());
    }

    @NatsStreamingSubscriber(subject = "topic.b", durableName = "test_durable")
    public void streamingHandler(io.nats.streaming.Message msg) {
        System.out.println(msg.getSubject());
    }
}
