package org.mvnsearch.spring.boot.nats.demo;

import io.nats.client.Message;
import org.mvnsearch.spring.boot.nats.NatsSubscriber;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
}
