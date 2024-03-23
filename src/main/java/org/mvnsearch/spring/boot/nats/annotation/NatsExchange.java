package org.mvnsearch.spring.boot.nats.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NatsExchange {
  String value() default "";

  String group() default "";

  /**
   * path for target topic, almost like path of RequestMapping
   *
   * @return path for topic name
   */
  String path() default "";

  String contentType() default "text/plain";
}
