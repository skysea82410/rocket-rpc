package com.roc.rocket.exception;

/**
 * @author roc
 * @date 2022/11/21
 */
public class ResponseTimeoutException extends RuntimeException {

    public ResponseTimeoutException() {
        super("服务响应超时");
    }
}
