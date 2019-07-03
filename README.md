Spring Boot Starter NATS
===========================
Spring Boot 2.0 starter for NATS with new java-nats 2.0 library.  For Spring Boot 1.x, please click https://github.com/linux-china/nats-spring-boot-starter/tree/1.x

### Why Spring Boot starter for nats
NATS is very simple, why you create a starter?

* Settings easy: just nats.url
* Spring Kafka like: @NatsSubscriber, @NatsStreamingSubscriber
* Metrics & endpoints: NATS states
* Health indicator for NATS

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

* in your code, use autowired client to send message

```
            @Autowired
            private Connection nats;
            ...
            nats.publish("topic.a","hello".getBytes());
```

* @NatsSubscriber support, method signature of subscriber is "(Message)->void"

```
@NatsSubscriber(subject = "topic.a")
    public void handler(Message msg) {

}
```

### NATS Streaming

* if you are using [NATS Streaming Server](https://nats.io/download/nats-io/nats-streaming-server/), please add setting in application.properties
```
...
nats.streaming.clusterId=test-cluster
nats.streaming.clientId=test-client
```

* in your code, use autowired client to send message

```
            @Autowired
            private StreamingConnection natsStreaming;
            ...
            natsStreaming.publish("topic.b", "hello streaming".getBytes());
```

* @NatsStreamingSubscriber support, method signature of subscriber is "(Message)->void"

```
    @NatsStreamingSubscriber(subject = "topic.b", durableName = "test_durable")
    public void streamingHandler(io.nats.streaming.Message msg) {
        System.out.println(msg.getSubject());
    }
``` 

### Beans by nats-spring-boot-starter

* io.nats.client.Connection: NATS connection
* io.nats.streaming.StreamingConnection: NATS Streaming connection
* endpoint: http://localhost:8080/actuator/nats
* endpoint: http://localhost:8080/actuator/streaming
* health indicator for NATS: http://localhost:8080/actuator/health


### Use Cases for Java Application

* Topic & Queue: of course
* Cloud Bus: configuration refresh
* Distribute Local JVM Cache Invalid:  easy now :)
* Registry: subscribe
* Request/Reply: message with reply. Spring Boot Application will subscribe topic named with "spring.application.name"
* Async Event

### Delay Queue

Get notification after delay expired.  First send the message to special subject with names "delay.15s", "delay.30s", "delay.60s", "delay.300s" with reply subject.
The delay queue brokers subscribed these subjects and store all the messages in the DelayQueue data structure. The delay broker will poll all delay queues, get expired messages,
and send them to the reply subject.


* Java: https://dzone.com/articles/changing-delay-and-hence-order
* Golang: https://github.com/dukex/squeue

### NGS integration
NGS is a global communications system built on NATS.io. NGS is easy to use, secure by default, and globally available in all major cloud providers. https://synadia.com/ngs/

Please add following configuration in your application.properties
```
nats.url = tls://connect.ngs.global:4222
nats.jwt-token= eyJ0eXAiOiJqd3QiLCJhbGciOiJlZDI1NTE5In0.xxxx.yyy
nats.nkey-token= xxxx
```

How to get jwt & nkey token?

* run 'ngs account status' to get account name and export with NGS_ACCOUNT

```
ngs account status
export NGS_ACCOUNT=ecstatic_bohr0
```

* jwt token
```
cat ~/.ngs/nats/synadia/accounts/${NGS_ACCOUNT}/users/${NGS_ACCOUNT}.jwt
```

* nkey token
```
cat ~/.nkeys/synadia/accounts/${NGS_ACCOUNT}/users/${NGS_ACCOUNT}.nk
```

### References

* NATS:  https://nats.io/
* NATS Docs: https://nats-io.github.io/docs/
* NATS Java client: https://github.com/nats-io/java-nats
* Method Handles in Java: http://www.baeldung.com/java-method-handles
* nats-operator: manage NATS clusters atop Kubernetes, automating their creation and administration: https://github.com/nats-io/nats-operator