package com.mola.redis.aspect;

import com.mola.redis.annotaion.FastRedisDelete;
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

/**
 * @author : molamola
 * @Project: springboot-redis
 * @Description:
 * @date : 2019-11-20 15:02
 **/
@Component
@Aspect
public class DeleteCacheAspect {

    @Autowired
    private RedisUtil redisUtil;

    @Pointcut("@annotation(com.mola.redis.annotaion.FastRedisDelete)")
    private void pointCut(){}

    @Around("pointCut()")
    private Object around(ProceedingJoinPoint joinPoint){
        // 获取方法
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        // 获取方法注解
        FastRedisDelete fastRedisDelete = method.getAnnotation(FastRedisDelete.class);
        String rawKey = fastRedisDelete.key();
        String namespace = FastRedisUtil.buildFinalNamespace(joinPoint, fastRedisDelete.namespace());
        Boolean allowKeyEmpty = fastRedisDelete.allowKeyEmpty();
        Boolean beforeInvocation = fastRedisDelete.beforeInvocation();

        // 构建redis-key
        String finalKey = FastRedisUtil.buildFinalKey(namespace,
                FastRedisUtil.finalKeyResolving(rawKey, method, joinPoint, allowKeyEmpty).toString());
        FastRedisUtil.doJudgeKeyEmpty(finalKey, allowKeyEmpty);

        // before
        if (beforeInvocation){
            redisUtil.del(finalKey);
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

        // after
        if (!beforeInvocation){
            redisUtil.del(finalKey);
        }
        return proceedResult;
    }
}
