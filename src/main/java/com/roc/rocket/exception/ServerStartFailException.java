package com.roc.rocket.exception;

/**
 * @author roc
 * @date 2022/11/7
 */
public class ServerStartFailException extends RuntimeException {

    public ServerStartFailException() {
        super("服务器启动失败");
    }
}
