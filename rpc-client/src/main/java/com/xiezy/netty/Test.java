package com.xiezy.netty;

import com.alibaba.fastjson.JSONObject;
import com.xiezy.entity.User;
import com.xiezy.rpc.DefaultFuture;
import com.xiezy.rpc.Request;
import com.xiezy.rpc.Response;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class Test {

    public static void main(String[] args) {

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            //设置分隔符
                            pipeline.addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()[0]));
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new ClientHandler());
                            pipeline.addLast(new StringEncoder());
                        }
                    });

            ChannelFuture connect = bootstrap.connect("127.0.0.1", 9999);

            User u = new User();
            u.setId(1L);
            u.setUserName("asd");

            Request request = new Request();
            request.setArgs(new Object[]{u});
            request.setMethod("com.xiezy.service.TestService.add");

            System.out.println("发送数据========================");
            connect.channel().writeAndFlush(JSONObject.toJSONString(request));
            connect.channel().writeAndFlush("\r\n");

            DefaultFuture reqFuture = new DefaultFuture(request);
            Response response = reqFuture.get();

            System.out.println(JSONObject.toJSON(response));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }

    }
}
