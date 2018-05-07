Spring Boot Starter NATS
===========================
Spring Boot 2.0 starter for NATS.  For Spring Boot 1.x, please click https://github.com/linux-china/nats-spring-boot-starter/tree/1.x

### Why Spring Boot starter for nats
NATS is very simple, why you create a starter?

* Settings easy
* Spring Kafka like
* Metrics & endpoints

### How to use

* please add following dependency in your pom.xml
```xml
          <dependency>
                 <groupId>org.mvnsearch.spring.boot</groupId>
                 <artifactId>nats-spring-boot-starter</artifactId>
                 <version>1.0.0-SNAPSHOT</version>
          </dependency>
```

* please add setting in application.properties. For cluster, please change url to "nats://host1:4222,nats://host2:4222"
```
nats.url = nats://localhost:4222
```

* in you code, use autowired client to send message

```
            @Autowired
            private Connection nats;
            ...
            nats.publish("topic.a","hello".getBytes());
```

* @NatsSubscriber support

```
@NatsSubscriber(subject = "topic.a")
    public void handler(Message msg) {

}
```

### Beans by nats-spring-boot-starter

* io.nats.client.Connection: NATS connection
* endpoint: http://localhost:8080/nats
* health indicator for NATS


### References

* NATS:  https://nats.io/
* NATS Java client: https://github.com/nats-io/java-nats
