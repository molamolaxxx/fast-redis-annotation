package com.mola.redis.utils;

import com.mola.redis.annotaion.FastRedisNamespace;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.Assert;

import java.lang.reflect.Method;

/**
 * @author : molamola
 * @Project: springboot-redis
 * @Description:
 * @date : 2019-11-19 19:52
 **/
public class FastRedisUtil {

    private static final char REDIS_NAMESPACE_SEPARATOR  = ':';

    public static String buildFinalKey(String namespace, String key){
        StringBuilder finalKey = new StringBuilder(namespace);
        // 如果命名空间不为空字符，则使用命名空间
        if (!"".equals(namespace)){
            finalKey.append(REDIS_NAMESPACE_SEPARATOR);
        }
        finalKey.append(key);

        return finalKey.toString();
    }

    public static String buildFinalNamespace(ProceedingJoinPoint joinPoint, String methodNamespace){
        // 获取类命名空间
        FastRedisNamespace classNamespace = joinPoint.getTarget().getClass()
                .getAnnotation(FastRedisNamespace.class);
        if (null != classNamespace){
            StringBuilder namespaceBuilder = new StringBuilder(classNamespace.namespace());
            namespaceBuilder.append(REDIS_NAMESPACE_SEPARATOR);
            namespaceBuilder.append(methodNamespace);
            return namespaceBuilder.toString();
        }
        return methodNamespace;
    }

    /**
     * 解析el表达式
     * @param rawKey 原始的key表达式
     * @param method
     * @param joinPoint
     * @return
     */
    public static Object finalKeyResolving(String rawKey, Method method,
                                           ProceedingJoinPoint joinPoint, Boolean allowKeyEmpty){
        ExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(rawKey);
        // 获取传入参数
        Object[] args = joinPoint.getArgs();
        LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
        // 获取参数名
        String[] parameterNames = discoverer.getParameterNames(method);

        Assert.isTrue(parameterNames.length == args.length, "k-v length is not match");
        // 传入参数键值对
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length ; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        Object spelResult = null;
        try {
            spelResult = expression.getValue(context);
        } catch (EvaluationException e) {
            return rawKey;
        }
        if (!allowKeyEmpty && null == spelResult){
            throw new RuntimeException("spel running with an empty result!");
        }
        return spelResult;
    }

    public static void doJudgeKeyEmpty(String finalKey, Boolean allowKeyEmpty){
        if (null == allowKeyEmpty && "".equals(finalKey) && !allowKeyEmpty){
            throw new RuntimeException("redis key can not be empty char!");
        }
    }
}
