package org.mvnsearch.spring.boot.nats;

import io.nats.client.Connection;
import io.nats.client.Nats;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

/**
 * nats test
 *
 * @author linux_china
 */
public class NatsTest {
  private static Connection nc;

  @BeforeAll
  public static void setUp() throws Exception {
    nc = Nats.connect("nats://localhost:4222");
  }

  @AfterAll
  public static void tearDown() throws Exception {
    nc.close();
  }

  @Test
  public void testStatus() throws Exception {
    System.out.println(nc.getStatus());
  }

  @Test
  public void testPubAndSub() throws Exception {
    nc.publish("subject1", "hello".getBytes(StandardCharsets.UTF_8));
    Thread.sleep(1000);
  }
}
