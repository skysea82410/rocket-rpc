package com.roc.rocket.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sky
 */
@Data
public abstract class ProtocolHeader implements Serializable {

    /**
     * 魔数
     */
    private int magicNumber = 1234;

    /**
     * 版本号
     */
    private int version = 1;

}
