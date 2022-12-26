package com.roc.rocket.encoder;

import com.roc.rocket.protocol.RocketResponseProtocol;
import com.roc.rocket.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author roc
 * @date 2022/11/3
 */
@Slf4j
public class RocketResponseEncoder extends MessageToByteEncoder<RocketResponseProtocol> {

    private Serializer serializer;

    public RocketResponseEncoder(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RocketResponseProtocol rocketResponseProtocol, ByteBuf byteBuf) throws Exception {
        log.debug("RocketResponse编码开始......");
        byte[] bytes = serializer.serialize(rocketResponseProtocol);
        int len = bytes.length;
        byteBuf.writeInt(len);
        byteBuf.writeBytes(bytes);
    }
}
