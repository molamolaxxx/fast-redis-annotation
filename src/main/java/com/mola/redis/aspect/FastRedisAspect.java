package com.mola.redis.aspect;

import com.mola.redis.annotaion.FastRedisCache;
import com.mola.redis.enmu.CacheType;
import com.mola.redis.utils.FastRedisUtil;
import com.mola.redis.utils.RedisUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author : molamola
 * @Project: springboot-redis
 * @Description:
 * @date : 2019-11-19 19:23
 * redis注解切面，可以完成数据的缓存读取
 **/
@Component
@Aspect
public class FastRedisAspect {

    @Autowired
    private RedisUtil redisUtil;

    @Pointcut("@annotation(com.mola.redis.annotaion.FastRedisCache)")
    private void pointCut(){}

    @Around("pointCut()")
    private Object around(ProceedingJoinPoint joinPoint){
        // 获取方法
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        // 获取方法注解
        FastRedisCache fastRedis = method.getAnnotation(FastRedisCache.class);

        // 获取注解中的命名空间与key
        String rawKey = fastRedis.key();
        String namespace = FastRedisUtil.buildFinalNamespace(joinPoint, fastRedis.namespace());
        Long timeout = fastRedis.timeout();
        TimeUnit unit = fastRedis.timeUnit();
        CacheType cacheType = fastRedis.cacheType();
        // 是否允许值为空
        Boolean allowKeyEmpty = fastRedis.allowKeyEmpty();

        // 构建redis-key
        String finalKey = FastRedisUtil.buildFinalKey(namespace,
                FastRedisUtil.finalKeyResolving(rawKey, method, joinPoint, allowKeyEmpty).toString());
        FastRedisUtil.doJudgeKeyEmpty(finalKey, allowKeyEmpty);
        /**
         * finalKey 不为空,取缓存,若为更新操作，这一步自动跳过
         */
        if (cacheType != CacheType.UPDATE) {
            Object searchResult = redisUtil.get(finalKey);
            if (null != searchResult) {
                // 如果查询内容不为空，则直接返回查到的内容
                return searchResult;
            }
        }

        /**
         * 执行具体逻辑
         */
        Object proceedResult = null;
        try {
            proceedResult = joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        /**
         * 执行缓存
         */
        doSetCache(finalKey, proceedResult, timeout, unit);

        return proceedResult;
    }

    /**
     * 设置缓存
     * @param finalKey
     * @param proceedResult
     * @param timeout
     * @param unit
     */
    private void doSetCache(String finalKey, Object proceedResult, Long timeout, TimeUnit unit){
        if (null != proceedResult) {
            redisUtil.set(finalKey, proceedResult);
        }
        if (timeout != -1L){
            redisUtil.expire(finalKey, timeout, unit);
        }
    }
}
