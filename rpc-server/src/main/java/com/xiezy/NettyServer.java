package com.xiezy;

import com.xiezy.constant.ZKConstant;
import com.xiezy.medium.Media;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.util.Set;

@Component
public class NettyServer implements ApplicationListener<ContextRefreshedEvent> {

    @Resource(name = "zkClient")
    private CuratorFramework zkClient;

    @Resource
    private NettyHandler nettyHandler;

    private ServerBootstrap serverBootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;

    private void runServer() throws Exception {
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap()
                .group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();

                        //设置分隔符
                        pipeline.addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()[0]));
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(nettyHandler);
                        pipeline.addLast(new StringEncoder());
                    }
                });

        ChannelFuture channelFuture = serverBootstrap.bind(9999).sync();
        //注册服务地址到zk
        InetAddress inetAddress = InetAddress.getLocalHost();
        zkClient.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(ZKConstant.SERVER_PATH + "/"
                                                    + inetAddress.getHostAddress() + "#");

        channelFuture.channel().closeFuture().sync();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (contextRefreshedEvent.getApplicationContext().getParent() == null) {
            try {
                Set<String> strings = Media.beanMethodMap.keySet();
                System.out.println("============================================");
                for (String str : strings){
                    System.out.println(str);
                }
                System.out.println("============================================");
                this.runServer();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                bossGroup.shutdownGracefully();
                workGroup.shutdownGracefully();
            }
        }
    }


}
