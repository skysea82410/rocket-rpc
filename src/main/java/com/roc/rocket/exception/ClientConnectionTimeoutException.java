package com.roc.rocket.exception;

/**
 * @author roc
 * @date 2022/11/1
 */
public class ClientConnectionTimeoutException extends RuntimeException {

    public ClientConnectionTimeoutException() {
        super("连接服务器超时");
    }
}
