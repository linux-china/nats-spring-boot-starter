package org.mvnsearch.spring.boot.nats.streaming;


import java.lang.annotation.*;

/**
 * NATS subscriber
 *
 * @author linux_china
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(NatsStreamingSubscribers.class)
public @interface NatsStreamingSubscriber {

    String subject();

    String queueGroup() default "";
}
