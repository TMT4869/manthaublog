package com.manthau.userservice.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        JsonMapper jsonMapper = JsonMapper.builder().build();

        tools.jackson.databind.JavaType javaType =
                jsonMapper.constructType(Object.class);

        JacksonJsonRedisSerializer<Object> serializer =
                new JacksonJsonRedisSerializer<>(jsonMapper, javaType);

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }
}