package com.roc.rocket.protocol;

import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Field;

@Data
public class RocketProtocolBody implements Serializable {

    /**
     * 完整类名
     */
    private String fullClassName;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 方法参数
     */
    private Field[] methodFields;

    /**
     * 方法值
     */
    private Object[] methodValues;

    /**
     * 返回值类型
     */
    private Class<?> returnValueType;

}
