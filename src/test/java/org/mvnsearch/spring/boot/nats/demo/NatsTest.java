package org.mvnsearch.spring.boot.nats.demo;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.nats.client.Connection;
import io.nats.streaming.StreamingConnection;

/**
 * nats test
 *
 * @author linux_china
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class NatsTest {
    @Autowired
    private Connection nats;
    
    @Autowired
    private StreamingConnection natsStreaming;

    @Test
    public void testConnect() {
        Assert.assertNotNull(nats);
    }

    @Test
    public void testPubAndSub() throws Exception {
        nats.publish("topic.a", "hello".getBytes());
        natsStreaming.publish("topic.b", "hello streaming".getBytes());
        Thread.sleep(1000);
    }
}
