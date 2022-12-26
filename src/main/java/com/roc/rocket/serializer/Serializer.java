package com.roc.rocket.serializer;

/**
 * @author roc
 * @date 2022/11/1
 */
public interface Serializer {

    byte[] serialize(Object o);

    <T> T deserialize(Class<?> clazz, byte[] bytes);
    
}
