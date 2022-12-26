package com.roc.rocket.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void setValue(String key, String value) {
        setValue(key, value, null);
    }

    public void setValue(String key, String value, Long expire) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        if (expire == null) {
            valueOperations.set(key, value);
        } else {
            valueOperations.set(key, value, expire, TimeUnit.SECONDS);
        }
    }

    public String getValue(String key) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object obj = valueOperations.get(key);
        return valueOperations.get(key) == null ? null : valueOperations.get(key).toString();
    }


}
