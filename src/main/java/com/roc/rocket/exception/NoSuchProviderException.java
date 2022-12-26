package com.roc.rocket.exception;

/**
 * @author roc
 * @date 2022/11/9
 */
public class NoSuchProviderException extends RuntimeException {

    public NoSuchProviderException() {
        super("未找到服务提供者");
    }
}
