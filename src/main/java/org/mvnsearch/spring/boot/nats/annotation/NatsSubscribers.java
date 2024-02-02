package org.mvnsearch.spring.boot.nats.annotation;

import java.lang.annotation.*;

/**
 * NATS subscribers annotation for repeat
 *
 * @author linux_china
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NatsSubscribers {
    NatsSubscriber[] value();
}
