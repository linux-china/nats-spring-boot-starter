package org.mvnsearch.spring.boot.nats.client;

import io.nats.client.Connection;
import io.nats.client.Nats;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mvnsearch.spring.boot.nats.demo.NatsDemoApplication;
import org.mvnsearch.spring.boot.nats.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = NatsDemoApplication.class)
public class UserServiceTest {

  @Autowired
  UserService userService;

  @Test
  public void testHello() throws Exception {
    User user = new User(1, "linux-china");
    String hello = userService.hello(user).block();
    System.out.println(hello);
  }

}
