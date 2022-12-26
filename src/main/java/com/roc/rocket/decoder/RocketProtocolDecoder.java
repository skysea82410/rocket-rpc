package com.roc.rocket.decoder;

import com.roc.rocket.protocol.RocketProtocol;
import com.roc.rocket.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author sky
 */
@Slf4j
public class RocketProtocolDecoder extends ByteToMessageDecoder {

    private static Integer MAGIC_NUMBER = 1234;

    private Serializer serializer;

    public RocketProtocolDecoder(Serializer serializer) {
        this.serializer = serializer;
    }

    /**
     * 协议解码
     * 备注：
     * 协议头共12字节：魔数占4字节，版本号占4字节，协议体长占4字节
     *
     * @param channelHandlerContext
     * @param byteBuf
     * @param list
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        log.debug("RocketProtocol解码开始......");

        //byteBuf的开头是一个int型表示长度的数字，所以一定不小于4个字节
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        int len = byteBuf.readInt();
        byte[] bytes = new byte[len];
        byteBuf.readBytes(bytes, 0, len);
        RocketProtocol rocketProtocol = serializer.deserialize(RocketProtocol.class, bytes);
        list.add(rocketProtocol);
    }
}
