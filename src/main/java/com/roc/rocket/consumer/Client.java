package com.roc.rocket.consumer;

import com.roc.rocket.consumer.handler.ConsumerMethodInboundHandler;
import com.roc.rocket.decoder.RocketResponseDecoder;
import com.roc.rocket.encoder.RocketProtocolEncoder;
import com.roc.rocket.exception.ClientConnectionTimeoutException;
import com.roc.rocket.protocol.RocketProtocol;
import com.roc.rocket.serializer.JsonSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * @author roc
 * @date 2022/11/1
 */
@Slf4j
public class Client {

    private final int MAX_RETRY = 2;

    public Boolean isConnection = false;

    public Boolean isConnecting = false;

    public Boolean isConnectError = false;

    private String ip;

    private Integer port;

    private EventLoopGroup eventLoopGroup;

    private Bootstrap bootstrap;

    private Channel channel;


    public Client(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    public synchronized void connect() {
        if (isConnecting || isConnection()) {
            return;
        }
        isConnecting = true;
        isConnectError = false;
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        ConsumerMethodInboundHandler consumerMethodInboundHandler = new ConsumerMethodInboundHandler(this);
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline channelPipeline = ch.pipeline();
                        channelPipeline.addLast(new RocketProtocolEncoder(new JsonSerializer()));
                        channelPipeline.addLast(new RocketResponseDecoder(new JsonSerializer()));
                        channelPipeline.addLast(consumerMethodInboundHandler);
                    }
                });
        connect(bootstrap, ip, port, MAX_RETRY);
    }

    private void connect(Bootstrap bootstrap, String ip, Integer port, Integer retry) {
        ChannelFuture channelFuture = bootstrap.connect(ip, port).addListener(future -> {
            if (future.isSuccess()) {
                log.info("连接服务器成功");
                isConnection = true;
                isConnecting = false;
            } else if (retry == 0) {
                isConnecting = false;
                isConnectError = true;
            } else {
                int delay = 1 << ((MAX_RETRY - retry) + 1);
                log.warn("连接服务器{}失败，端口：{}，正在重试......", ip, port);
                bootstrap.config().group().schedule(() -> connect(bootstrap, ip, port, retry - 1), delay, TimeUnit.SECONDS);
            }
        });
        channel = channelFuture.channel();
    }

    public Boolean isConnection() {
        return isConnection;
    }

    public void setDisconnection() {
        this.isConnection = false;
    }

    public Boolean isConnectionServerError() {
        return isConnectError;
    }

    public Boolean isClientReady() throws Exception {
        //客户端进行远程连接，如果正在连接或已经连接成功，则忽略
        this.connect();
        //如果客户端没有连接成功，会自动进行重试，所以这里只判断是否连接成功
        // ，如果没有并且客户端还在重试，则进行循环等待
        while (!isConnection() && !isConnectionServerError()) {
            TimeUnit.SECONDS.sleep(1);
        }
        //如果重试后仍然失败，则抛出连接异常
        if (isConnectionServerError()) {
            throw new ClientConnectionTimeoutException();
        }
        return true;
    }

    public Object call(RocketProtocol rocketProtocol) throws Exception {
        //判断连接是否准备好
        if (isClientReady()) {
            channel.writeAndFlush(rocketProtocol);
            //返回响应结果
            return ConsumerMethodInboundHandler.getResponse(rocketProtocol.getId());
        }
        return null;
    }

    @PreDestroy
    public void close() {
        log.info("客户端正在关闭......");
        eventLoopGroup.shutdownGracefully();
        channel.closeFuture().syncUninterruptibly();
    }


}
