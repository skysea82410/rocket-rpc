package com.roc.rocket.serializer;


import com.alibaba.fastjson.JSON;

/**
 * @author roc
 * @date 2022/11/1
 */
public class JsonSerializer implements Serializer {

    @Override
    public byte[] serialize(Object o) {
        return JSON.toJSONBytes(o);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        return JSON.parseObject(bytes, clazz);
    }
}
