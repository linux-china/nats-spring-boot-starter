package org.mvnsearch.spring.boot.nats.demo;

import io.nats.client.Message;
import org.mvnsearch.spring.boot.nats.NatsSubscriber;
import org.mvnsearch.spring.boot.nats.streaming.NatsStreamingSubscriber;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * NATS demo application
 *
 * @author linux_china
 */
@SpringBootApplication
@ComponentScan("org.mvnsearch.spring.boot.nats")
public class NatsDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(NatsDemoApplication.class, args);
    }

    @NatsSubscriber(subject = "topic.a")
    public void handler(Message msg) {
        System.out.println(msg.getSubject());
    }
    
    @NatsStreamingSubscriber(subject = "topic.b")
    public void streamingHandler(io.nats.streaming.Message msg) {
    	System.out.println(msg.getSubject());
    }
}
