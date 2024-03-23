package org.mvnsearch.spring.boot.nats.client;

import io.nats.client.Connection;
import io.nats.client.Nats;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mvnsearch.spring.boot.nats.model.User;

public class UserServiceTest {
  private static Connection nc;
  private static UserService userService;

  @BeforeAll
  public static void setUp() throws Exception {
    nc = Nats.connect("nats://localhost:4222");
    userService = NatsExchangeProxyFactory.buildStub(nc, UserService.class);
  }

  @AfterAll
  public static void tearDown() throws Exception {
    nc.close();
  }

  @Test
  public void testHello() throws Exception {
    User user = new User(1, "linux-china");
    String hello = userService.hello(user).block();
    System.out.println(hello);
  }

}
