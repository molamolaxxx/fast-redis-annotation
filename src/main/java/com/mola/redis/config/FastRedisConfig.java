package com.mola.redis.config;

import com.mola.redis.utils.FastJson2JsonRedisSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author : molamola
 * @Project: springboot-redis
 * @Description: redisconfig
 * @date : 2019-10-16 15:13
 **/
@Configuration
public class FastRedisConfig {

    @Value("${fast-redis.use-default-redisTemplate}")
    private Boolean useDefaultRedisTemplate = true;

    /**
     * springboot2.x 使用LettuceConnectionFactory 代替 RedisConnectionFactory
     * application.yml配置基本信息后,springboot2.x  RedisAutoConfiguration能够自动装配
     * LettuceConnectionFactory 和 RedisConnectionFactory 及其 RedisTemplate
     *
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory redisConnectionFactory){
        if (useDefaultRedisTemplate) {
            RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
            //开启事务
            redisTemplate.setEnableTransactionSupport(true);

            //v序列化方式，采用json
            FastJson2JsonRedisSerializer serializer = new FastJson2JsonRedisSerializer(Object.class);

            //key序列化方式，采用string
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            //键的序列化Sring
            redisTemplate.setValueSerializer(serializer);

            redisTemplate.setHashKeySerializer(new StringRedisSerializer());
            redisTemplate.setHashValueSerializer(serializer);
            redisTemplate.setDefaultSerializer(new StringRedisSerializer());
            //设置连接池，此处使用Luttuce
            redisTemplate.setConnectionFactory(redisConnectionFactory);
            redisTemplate.afterPropertiesSet();
            return redisTemplate;
        }
        return null;
    }
}
