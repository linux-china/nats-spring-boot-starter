package org.mvnsearch.spring.boot.nats.streaming;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * NATS Streaming subscribers annotation for repeat
 *
 * @author wisni
 */
@Target({ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NatsStreamingSubscribers {

    NatsStreamingSubscriber[] value();
}
