package com.roc.rocket.exception;

/**
 * @author roc
 * @date 2022/11/8
 */
public class ProducerLoadFailException extends RuntimeException {

    public ProducerLoadFailException() {
        super("生产者信息加载失败");
    }
}
