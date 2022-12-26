package com.roc.rocket.registry.provider;

import lombok.Data;

/**
 * @author roc
 * @date 2022/11/9
 */
@Data
public class Provider {

    /**
     * 接口名称
     */
    private String api;

    /**
     * 提供者名称
     */
    private String provider;

    /**
     * 提供者ip
     */
    private String ip;

    /**
     * 提供者端口号
     */
    private Integer port;

}
