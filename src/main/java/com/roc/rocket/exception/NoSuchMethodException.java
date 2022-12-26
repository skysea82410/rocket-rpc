package com.roc.rocket.exception;

/**
 * @author roc
 * @date 2022/11/7
 */
public class NoSuchMethodException extends RuntimeException {

    public NoSuchMethodException() {
        super("执行方法不存在");
    }
}
