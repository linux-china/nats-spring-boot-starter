package org.mvnsearch.spring.boot.nats.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NatsService {
    String name();

    String version();

    String description();

    String format() default "json";

}
