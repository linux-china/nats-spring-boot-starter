package org.mvnsearch.spring.boot.nats.streaming;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * NATS Streaming subscriber
 *
 * @author wisni
 */
@Target({ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(NatsStreamingSubscribers.class)
public @interface NatsStreamingSubscriber {

    String subject();

    String queueGroup() default "";

    String durableName() default "";

    boolean manualAcks() default false;

    String ackWait() default "";

    String maxInFlight() default "";
}
