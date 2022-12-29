package com.roc.rocket.serializer;

import com.roc.rocket.utils.ProtostuffUtils;

/**
 * @author roc
 * @date 2022/12/29
 */
public class ProtostuffSerializer implements Serializer {

    @Override
    public byte[] serialize(Object o) {
        return ProtostuffUtils.serialize(o);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        return ProtostuffUtils.deserialize(bytes, clazz);
    }
}
