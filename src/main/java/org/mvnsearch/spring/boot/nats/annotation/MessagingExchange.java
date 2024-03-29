package org.mvnsearch.spring.boot.nats.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessagingExchange {
    String value();

    String format() default "application/json";
}
