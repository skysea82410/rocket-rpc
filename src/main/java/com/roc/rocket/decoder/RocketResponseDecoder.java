package com.roc.rocket.decoder;

import com.roc.rocket.protocol.RocketResponseProtocol;
import com.roc.rocket.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author roc
 * @date 2022/11/3
 */
@Slf4j
public class RocketResponseDecoder extends ByteToMessageDecoder {

    private Serializer serializer;

    public RocketResponseDecoder(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        log.debug("RocketResponse解码开始......");

        //byteBuf的开头是一个int型表示长度的数字，所以一定不小于4个字节
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        int len = byteBuf.readInt();
        byte[] bytes = new byte[len];
        byteBuf.readBytes(bytes, 0, len);
        RocketResponseProtocol rocketResponseProtocol = serializer.deserialize(RocketResponseProtocol.class, bytes);
        list.add(rocketResponseProtocol);
    }
}
