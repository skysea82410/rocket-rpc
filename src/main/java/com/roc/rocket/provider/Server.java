package com.roc.rocket.provider;

import com.roc.rocket.decoder.RocketProtocolDecoder;
import com.roc.rocket.encoder.RocketResponseEncoder;
import com.roc.rocket.exception.ServerStartFailException;
import com.roc.rocket.provider.handler.ProviderChannelHandler;
import com.roc.rocket.serializer.ProtostuffSerializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class Server {

    @Value("${roc.rocket.server.port}")
    private Integer port;

    /**
     * 最大重试次数
     */
    @Value("${roc.rocket.server.max-retry}")
    private Integer maxRetry;

    /**
     * 是否正在启动服务
     */
    private static Boolean starting = false;
    /**
     * 服务是否启动完成
     */
    private static Boolean started = false;

    private ServerBootstrap serverBootstrap;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workGroup;

    private Channel channel;

    @Resource
    private ProviderChannelHandler providerChannelHandler;

    @PostConstruct
    public synchronized void start() {
        if (!canStart()) {
            return;
        }
        starting = true;
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline channelPipeline = ch.pipeline();
                        channelPipeline.addLast(new RocketProtocolDecoder(new ProtostuffSerializer()));
                        channelPipeline.addLast(new RocketResponseEncoder(new ProtostuffSerializer()));
                        channelPipeline.addLast(providerChannelHandler);

                    }
                });
        try {
            channel = start(serverBootstrap, port);
        } catch (Exception e) {
            log.error("rocket server exception :{}", e.getMessage());
        }
    }

    private Channel start(ServerBootstrap serverBootstrap, int port) {
        ChannelFuture channelFuture = serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()) {
                log.info("Rocket Server started , port is {}", port);
                setStarted();
            } else if (maxRetry == 0) {
                throw new ServerStartFailException();
            } else {
                maxRetry--;
                int delay = 1 << (maxRetry + 1);
                serverBootstrap.config().group().schedule(() -> start(serverBootstrap, port), delay, TimeUnit.SECONDS);
            }
        });
        return channelFuture.channel();
    }

    private Boolean canStart() {
        return !(starting || started);
    }

    private void setStarted() {
        starting = false;
        started = true;
    }


    @PreDestroy
    private void shutdown() {
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}