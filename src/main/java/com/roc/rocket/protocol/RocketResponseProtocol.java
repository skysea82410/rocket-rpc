package com.roc.rocket.protocol;

import lombok.Data;

/**
 * @author roc
 * @date 2022/11/1
 */
@Data
public class RocketResponseProtocol {

    private String id;

    private String requestId;

    private Object response;

}
