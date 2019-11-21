package com.mola.redis.annotaion;

import com.mola.redis.enmu.CacheType;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FastRedisCache {

    String namespace() default "";

    String key() default "";

    long timeout() default -1L;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    // 是否允许缓存值为空
    boolean allowKeyEmpty() default false;

    CacheType cacheType() default CacheType.NORMAL;
}
