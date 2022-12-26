package com.roc.rocket.consumer.handler;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.roc.common.utils.json.JsonUtils;
import com.roc.rocket.consumer.Client;
import com.roc.rocket.exception.ResponseTimeoutException;
import com.roc.rocket.future.ResponseFuture;
import com.roc.rocket.protocol.RocketProtocol;
import com.roc.rocket.protocol.RocketResponseProtocol;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author roc
 * @date 2022/10/31
 */
@Slf4j
@ChannelHandler.Sharable
public class ConsumerMethodInboundHandler extends ChannelDuplexHandler {

    private final static Map<String, ResponseFuture> requestMap = Maps.newConcurrentMap();

    private final static Long RESPONSE_TIMEOUT = 50L;

    private Client client;

    public ConsumerMethodInboundHandler(Client client) {
        this.client = client;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("收到服务端回执数据：{}", JsonUtils.toJson(msg));
        if (msg instanceof RocketResponseProtocol) {
            RocketResponseProtocol responseProtocol = (RocketResponseProtocol) msg;
            if (requestMap.containsKey(responseProtocol.getRequestId())) {
                ResponseFuture responseFuture = requestMap.get(responseProtocol.getRequestId());
                responseFuture.setRocketResponse(responseProtocol);
            }
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        log.info("发送数据：{}", JSON.toJSON(msg));
        if (msg instanceof RocketProtocol) {
            RocketProtocol rocketProtocol = (RocketProtocol) msg;
            requestMap.putIfAbsent(rocketProtocol.getId(), new ResponseFuture());
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("与服务器断开连接");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        client.setDisconnection();
    }


    public static Object getResponse(String requestId) {
        //每个请求都有唯一的一个requestId，通过isReady来判断请求是否准备好，即是否被发送成功
        if (!isReady(requestId)) {
            throw new ResponseTimeoutException();
        }

        try {
            ResponseFuture responseFuture = requestMap.get(requestId);
            //等待返回调用结果
            RocketResponseProtocol responseProtocol = responseFuture.getRocketResponse(RESPONSE_TIMEOUT);
            if (responseProtocol == null) {
                return null;
            }
            return responseProtocol.getResponse();
        } finally {
            requestMap.remove(requestId);
        }
    }

    private static Boolean isReady(String requestId) {
        Boolean isReady = false;
        int retry = 5;

        try {
            while (!requestMap.containsKey(requestId) && retry > 0) {
                TimeUnit.MILLISECONDS.sleep(RESPONSE_TIMEOUT);
                --retry;
            }
            if (retry > 0) {
                isReady = true;
            }
        } catch (Exception e) {
            return false;
        }
        return isReady;
    }
}
