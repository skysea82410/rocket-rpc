package com.roc.rocket.provider;

import com.roc.rocket.utils.NetUtils;
import lombok.Data;

/**
 * @author roc
 * @date 2022/11/8
 */
@Data
public class RocketProviderDefinition {

    /**
     * 接口名称
     */
    private String apiName;

    /**
     * 实现类
     */
    private Class<?> clazz;

    /**
     * 实现类实例
     */
    private Object instance;

    /**
     * 提供者名称
     */
    private String provider;

    /**
     * 提供者ip
     */
    private String ip;

    /**
     * 提供者端口
     */
    private Integer port;

    public String getIp() {
        return NetUtils.getLocalIpAddr();
    }
}
