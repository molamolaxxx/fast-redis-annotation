package com.mola.redis.annotaion;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FastRedisDelete {
    String namespace() default "";

    String key() default "";

    boolean beforeInvocation() default false;

    // 是否允许缓存值为空
    boolean allowKeyEmpty() default false;
}
