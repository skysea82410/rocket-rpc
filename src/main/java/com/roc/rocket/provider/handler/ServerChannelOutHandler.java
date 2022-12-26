package com.roc.rocket.provider.handler;


import com.roc.common.utils.json.JsonUtils;
import com.roc.rocket.protocol.RocketResponseProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

/**
 * @author roc
 * @date 2022/10/27
 */
@Slf4j
public class ServerChannelOutHandler extends ChannelOutboundHandlerAdapter {


    @Override
    public void write(ChannelHandlerContext channelHandlerContext, Object o, ChannelPromise channelPromise) throws Exception {

        if (o instanceof RocketResponseProtocol) {
            log.info("Server Write :{}", JsonUtils.toJson(o));
            channelHandlerContext.writeAndFlush(o);
            channelHandlerContext.flush();
        }
    }


}
