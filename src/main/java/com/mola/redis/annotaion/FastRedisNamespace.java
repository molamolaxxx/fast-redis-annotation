package com.mola.redis.annotaion;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FastRedisNamespace {
    String namespace() default "";
}
