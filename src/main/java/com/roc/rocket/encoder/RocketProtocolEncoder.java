package com.roc.rocket.encoder;

import com.roc.rocket.protocol.RocketProtocol;
import com.roc.rocket.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class RocketProtocolEncoder extends MessageToByteEncoder<RocketProtocol> {

    private Serializer serializer;

    public RocketProtocolEncoder(Serializer serializer) {
        this.serializer = serializer;
    }

    /**
     * 协议编码
     * 备注：
     * 协议头共12字节：魔数占4字节，版本号占4字节，协议体长占4字节
     *
     * @param channelHandlerContext
     * @param rocketProtocol
     * @param byteBuf
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RocketProtocol rocketProtocol, ByteBuf byteBuf) throws Exception {
        log.debug("RocketProtocol编码开始......");
        byte[] data = serializer.serialize(rocketProtocol);
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }


}
