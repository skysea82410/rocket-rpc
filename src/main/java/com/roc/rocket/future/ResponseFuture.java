package com.roc.rocket.future;

import com.roc.rocket.protocol.RocketResponseProtocol;
import lombok.extern.slf4j.Slf4j;

/**
 * @author roc
 * @date 2022/11/1
 */
@Slf4j
public class ResponseFuture {

    private RocketResponseProtocol rocketResponse;

    private volatile Boolean success = false;

    private static final Object object = new Object();

    public RocketResponseProtocol getRocketResponse(Long timeout) {
        synchronized (object) {
            while (!success) {
                try {
                    object.wait(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return rocketResponse;
        }
    }

    public void setRocketResponse(RocketResponseProtocol rocketResponse) {
        if (success) {
            return;
        }
        synchronized (object) {
            this.rocketResponse = rocketResponse;
            success = true;
            object.notify();
        }
    }
}
