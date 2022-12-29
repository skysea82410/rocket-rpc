package com.roc.rocket.protocol;

import lombok.Data;

/**
 * @author roc
 * @date 2022/11/1
 */
@Data
public class RocketResponseProtocol {

    /**
     * reponse id
     */
    private String id;

    /**
     * 请求id
     */
    private String requestId;

    /**
     * 返回值
     */
    private Object response;

    /**
     * 返回值类型
     */
    private Class<?> responseType;

}
