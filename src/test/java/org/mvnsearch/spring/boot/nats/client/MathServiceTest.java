package org.mvnsearch.spring.boot.nats.client;

import io.nats.client.Connection;
import io.nats.client.Nats;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MathServiceTest {
  private static Connection nc;
  private static MathService mathService;

  @BeforeAll
  public static void setUp() throws Exception {
    nc = Nats.connect("nats://localhost:4222");
    mathService = NatsExchangeProxyFactory.buildStub(nc, MathService.class);
  }

  @AfterAll
  public static void tearDown() throws Exception {
    nc.close();
  }

  @Test
  public void testServiceCall() throws Exception {
    Integer min = mathService.min("1,2").block();
    System.out.println(min);
  }

  @Test
  public void testPost() throws Exception {
    mathService.post("Hello");
  }

}
