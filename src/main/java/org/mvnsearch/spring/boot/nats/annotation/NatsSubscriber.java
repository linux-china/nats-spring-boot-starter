package org.mvnsearch.spring.boot.nats.annotation;


import org.mvnsearch.spring.boot.nats.annotation.NatsSubscribers;

import java.lang.annotation.*;

/**
 * NATS subscriber
 *
 * @author linux_china
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(NatsSubscribers.class)
public @interface NatsSubscriber {

    String subject();

    String queueGroup() default "";
}
