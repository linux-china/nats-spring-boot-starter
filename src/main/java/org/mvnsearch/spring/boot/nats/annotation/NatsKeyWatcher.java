package org.mvnsearch.spring.boot.nats.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NatsKeyWatcher {
    String key();

    String bucket();

    String format() default "application/json";
}
