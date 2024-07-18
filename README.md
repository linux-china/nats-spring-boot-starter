Spring Boot Starter NATS
===========================
Spring Boot 2.x/3.x starter for NATS with Publish/Subscribe, Services Framework, JetStream KV watch support.

![architecture](architecture.png)

# Why Spring Boot starter for NATS?

NATS is very simple, why you create a starter for Spring Boot?

* Nats Microservices framework support: RPC style with `json`, `protobuf`, `avro` data format.
* JetStream KeyValue watch support: durable Component and state sync between instances.
* NATS service interface: almost alike Spring HTTP interface to make services call easy
* Spring Kafka like: `@NatsSubscriber` to listen subject
* Subject(UUID) for instance only: make A/B easy
* NatsTemplate: friendly with Spring `XxxTemplate` style
* Metrics & endpoints: `/actuator/nats`, NATS information/statistics
* Health indicator for NATS
* NATS Protocol Binding for CloudEvents

# Get Started with Publish/Subscribe

* please add following dependency in your pom.xml

```xml

<dependency>
  <groupId>org.mvnsearch</groupId>
  <artifactId>nats-spring-boot-starter</artifactId>
  <version>0.1.1</version>
</dependency>
```

* please add setting in application.properties. For cluster, please change url to "nats://host1:4222,nats://host2:4222"

```
nats.spring.server = nats://localhost:4222
nats.spring.connection-name=${spring.application.name}.${random.uuid}
```

* in you code, use NatsTemplate to send message

```
   @Autowired
   private NatsTemplate natsTemplate;
   ...
   natsTemplate.publish("subject1","hello".getBytes());
```

* @NatsSubscriber support, method signature of subscriber is `"(Message)->void"`

```
    @NatsSubscriber(subject = "topic.a")
    public void handler(Message msg) {
}
```

### Subject for App Instance only

Every app instance will listen a subject, such as `app-name-75454360-49f0-4609-9ed9-1e3bef4219cc`(print on console),
and you can send the messages to the subject and communicate with the instance only.

It's easy and simple, and you can use `AppInstanceOnlyMessageHandler` interface to handle the message.

```java

@SpringBootApplication
public class NatsDemoApplication implements AppInstanceOnlyMessageHandler {

  public static void main(String[] args) {
    SpringApplication.run(NatsDemoApplication.class, args);
  }

  @Override
  public void onMessage(Message msg) throws InterruptedException {
    System.out.println("Received message from:" + msg.getSubject());
  }
}
```

Some use cases for this feature:

* Peer-to-Peer communication: send messages to a specific instance.
* A/B testing: send the message to a specific instance, and not broadcast to all instances.


# NATS MicroServices framework

[NATS Services Framework](https://natsbyexample.com/examples/services/intro/java) is MicroServices RPC framework with
Service Discovery support,
and you can
check [NATS Service API Spec](https://github.com/nats-io/nats-architecture-and-design/blob/main/adr/ADR-32.md) for
detail.

### How to publish NATS MicroServices?

In the server side, create a normal Spring Boot controller with `@MessageMapping` and `@NatsService` annotations.

```java

@Controller
@MessageMapping("minmax")
@NatsService(name = "minmax", version = "0.0.1", description = "min/max number service")
public class UserNatsService {

  @MessageMapping("min")
  public int min(@Payload String body) {
    int min = Integer.MAX_VALUE;
    String[] input = body.split(",");
    for (String n : input) {
      min = Math.min(min, Integer.parseInt(n));
    }
    return min;
  }
}
```

Please
refer [MessageMapping](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/messaging/handler/annotation/MessageMapping.html)
for arguments binding. Now only following annotations supported:

* `@Payload String body` or `String body`: bind the message body to the method parameter
* `@Header("contentType") String contentType`: bind the message header to the method parameter
* `@Headers Map<String, Object> headers`: bind the destination variable to the method parameter

After server started, and you can use `nats micro ls` to check services, and use `nats request minmax.min "1,2"` to make
a test.

### How to consume NATS MicroServices?

In the client side, create an interface with `@NatsExchange` and add methods with `@ServiceExchange` annotations.

```java

@NatsExchange
public interface UserService {

  @ServiceExchange("minmax.min")
  Mono<Integer> min(String text);
}
```

Then to build service stub proxy to call the service.

```java
public class UserServiceTest {

  @Test
  public void testServiceCall() throws Exception {
    Connection nc = Nats.connect("nats://localhost:4222");
    UserService userService = NatsExchangeProxyFactory.buildStub(nc, UserService.class);
    Integer min = userService.min("1,2").block();
    System.out.println(min);
  }
}
```

**Attention**: NATS MicroServices frameworks is based on `request-reply` model, and it is async mode, and you need to
use Reactive `Mono` to handle the result.

### Object serialization for NATS Services

If you want to use JSON, and you can use `@NatsExchange` annotation to specify the content type as following:

```java

@NatsExchange(value = "nats://localhost:4222", path = "UserService", contentType = "application/json")
public interface UserService {
  @ServiceExchange("hello")
  Mono<String> hello(User user);
}
```

The following content types are supported:

* Jackson(application/json):  https://github.com/FasterXML/jackson
* Protobuf(application/protobuf): https://developers.google.com/protocol-buffers/
* Avro(application/avro): https://avro.apache.org/ friendly with Kafka Schema

If you choose Protobuf, and it's better to add `content-type` and `message-type` headers in NATS message.

```
content-type: application/x-protobuf
message-type: org.mvnsearch.User
```

# Durable Component

Durable component is almost like [Cloudflare Durable Objects](https://developers.cloudflare.com/durable-objects/),
and you can use NATS JetStream KV watch to sync states between instances.

```java

@Component
@NatsDurableComponent
public class OnlineUserComponent {
  private static final Logger logger = LoggerFactory.getLogger(OnlineUserComponent.class);
  private int onlineUserCount;

  @NatsKeyWatcher(bucket = "bucket", key = "online.user.count")
  public void setOnlineUserCount(int count) {
    logger.info("Online user count: " + count);
    this.onlineUserCount = count;
  }
}
```

After `bucket/online.user.count` key changed, the `setOnlineUserCount` method will be called to sync the state between
instances.

**Tips**: If you have a state with many fields, and you can use JavaBean or `record` as fields.
Then call `nats kv put bucket online.admin '{"nick": "linux_china", "phone":"138xxx"}'` to update JavaBean state.

# NATS KeyValue store

[NATS KeyValue Store](https://docs.nats.io/nats-concepts/jetstream/key-value-store) is a nice feature for some cases:

* Configuration Data: watch support
* Metadata: Data Schema, such as avro, protobuf, json schema etc under `schema-registry` bucket.
* State data: cooperate with instances, such as rate limit, black list etc.

# NATS Object Store

[NATS Object Store](https://docs.nats.io/nats-concepts/jetstream/obj_store) is a nice feature for some cases:

* Workload binary storage: such as Wasm module, JS bundle etc.
* Statics Data: some data for report(short-time), such as CSV, JSON etc.
* Callback support: watch support for bucket.

# Spring Cloud Stream Binder for NATS

Use official Spring Cloud Stream Binder for
NATS [nats-spring-cloud-stream-binder](https://github.com/nats-io/spring-nats).

**Tips**: nats-spring-boot-starter is based on spring-nats, and you can use both of them in your project.

# Spring Boot Actuator

Please visit `/actuator/nats` for NATS information and statistics.

* NATS Server Information
* NATS Services
* Nats Subscribers
* Disconnect from NATS: Graceful shutdown - `POST /actuator/nats/offline`

# NATS Protocol Binding for CloudEvents

Please refer [NATS Protocol Binding for CloudEvents](https://github.com/cloudevents/spec/blob/main/cloudevents/bindings/nats-protocol-binding.md).

NATS Message's `Content-Type` header value: `application/cloudevents+json`。

Convert NATS Message to CloudEvent:

```
    EventFormat format = EventFormatProvider.getInstance().resolveFormat(JsonFormat.CONTENT_TYPE);
    if (msg.getHeaders().getFirst("Content-Type").equals(format.serializedContentType())) {
       final CloudEvent cloudEvent = format.deserialize(msg.getData());
    }
```

# GraphQL over NATS

GraphQL service interface definition.

```
public interface GraphqlService {

    Mono<String> query(String query, Map<String, Object> variables);

    Mono<String> mutation(String mutation, Map<String, Object> variables);

    Flux<String> subscription(String subscription, Map<String, Object> variables);
}
```

### query/mutation
Services Framework(request-reply)

### subscription

* Subscribe a subject, such as "graphql.sub.xxxx"
* Send request-reply, message with "subscribed-to" header, and the value is the subject
* Received the replied message. If payload is ok, then you can receive the subscription message. otherwise, unsubscribe the subject.

# Todo

* Data schema metadata for services: such as json schema, load schema file from classpath or annotation?
* Graceful shutdown: call actuator endpoint to disconnect from NATS?

# References

* NATS:  https://nats.io/
* NATS Docs: https://docs.nats.io/
* NATS Architecture and Design Docs: https://github.com/nats-io/nats-architecture-and-design
* NATS Java client: https://github.com/nats-io/nats.java
* spring-nats: https://github.com/nats-io/spring-nats
* Monitoring NATS: https://docs.nats.io/running-a-nats-service/nats_admin/monitoring
* Method Handles in Java: http://www.baeldung.com/java-method-handles
* EventCatalog: Documentation tool for Event-Driven Architectures - https://www.eventcatalog.dev/
