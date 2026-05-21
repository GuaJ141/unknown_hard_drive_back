package com.unknownharddrivesystem.config;

import com.unknownharddrivesystem.utils.FastJson2RedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class RedisConfig{

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        FastJson2RedisSerializer<Object> serializer = new FastJson2RedisSerializer<>(Object.class);

        // key 采用 String 序列化方式
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        // hash 的 key 也采用 String 序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}