package com.roc.rocket.utils;

import java.util.UUID;

/**
 * @author roc
 * @date 2022/11/1
 */
public class UuidUtils {

    public static String createId() {
        return UUID.randomUUID().toString();
    }
}
