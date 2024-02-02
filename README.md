Spring Boot Starter NATS
===========================
Spring Boot 2.x/3.x starter for NATS with Publish/Subscribe, Services Framework, JetStream KV.

### Why Spring Boot starter for nats
NATS is very simple, why you create a starter?

* Nats Microservices framework support
* JetStream KV watch support
* NATS service interface
* Spring Kafka like: @NatsSubscriber
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
nats.spring.server = nats://localhost:4222
```

* in you code, use autowired client to send message

```
            @Autowired
            private Connection nats;
            ...
            nats.publish("topic.a","hello".getBytes());
```

* @NatsSubscriber support,  method signature of subscriber  is "(Message)->void"

```
    @NatsSubscriber(subject = "topic.a")
    public void handler(Message msg) {

}
```

### Beans by nats-spring-boot-starter

* io.nats.client.Connection: NATS connection
* endpoint: http://localhost:8080/actuator/nats
* health indicator for NATS: http://localhost:8080/actuator/health


### Use Cases for Java Application

* Topic & Queue: of course
* Cloud Bus: configuration refresh
* Distribute Local JVM Cache Invalid:  easy now :)
* Registry: subscribe
* MicroServices: NATS Microservices framework
* Async Event

### Delay Queue

Get notification after delay expired.  First send the message to special subject with names "delay.15s", "delay.30s", "delay.60s", "delay.300s" with reply subject.
The delay queue brokers subscribed these subjects and store all the messages in the DelayQueue data structure. The delay broker will poll all delay queues, get expired messages,
and send them to the reply subject.


* Java: https://dzone.com/articles/changing-delay-and-hence-order
* Golang: https://github.com/dukex/squeue

### References

* NATS:  https://nats.io/
* NATS Docs: https://nats-io.github.io/docs/
* Introducing NATS CLI: https://nats.io/blog/nats-cli-intro/
* NATS Java client: https://github.com/nats-io/java-nats
* Method Handles in Java: http://www.baeldung.com/java-method-handles
* nats-operator: manage NATS clusters atop Kubernetes, automating their creation and administration: https://github.com/nats-io/nats-operator
* Contrasting NATS with Apache Kafk: https://itnext.io/contrasting-nats-with-apache-kafka-1d3bdb9aa767
