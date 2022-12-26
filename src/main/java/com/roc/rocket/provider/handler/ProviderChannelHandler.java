package com.roc.rocket.provider.handler;

import com.roc.common.utils.json.JsonUtils;
import com.roc.rocket.protocol.RocketProtocol;
import com.roc.rocket.protocol.RocketProtocolBody;
import com.roc.rocket.protocol.RocketResponseProtocol;
import com.roc.rocket.provider.RocketProviderExecutor;
import com.roc.rocket.utils.UuidUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author roc
 * @date 2022/10/31
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class ProviderChannelHandler extends SimpleChannelInboundHandler<RocketProtocol> {


    @Resource
    private RocketProviderExecutor rocketProducerExecutor;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RocketProtocol rocketProtocol) throws Exception {
        log.info("收到请求数据:{}", JsonUtils.toJson(rocketProtocol));
        //解析协议体
        RocketProtocolBody rocketProtocolBody = rocketProtocol.getRocketProtocolBody();
        //执行调用
        Object returnValue = rocketProducerExecutor.call(rocketProtocolBody.getFullClassName(), rocketProtocolBody.getMethodName(), rocketProtocolBody.getMethodValues());
        log.info("返回数据：{}", JsonUtils.toJson(returnValue));
        //封装请求响应协议
        RocketResponseProtocol rocketResponseProtocol = new RocketResponseProtocol();
        rocketResponseProtocol.setId(UuidUtils.createId());
        rocketResponseProtocol.setRequestId(rocketProtocol.getId());
        rocketResponseProtocol.setResponse(returnValue);
        channelHandlerContext.writeAndFlush(rocketResponseProtocol);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端{}已断开", ctx.channel().id());
        super.channelInactive(ctx);
    }
}
