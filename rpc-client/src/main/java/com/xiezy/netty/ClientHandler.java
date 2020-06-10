package com.xiezy.netty;

import com.alibaba.fastjson.JSONObject;
import com.xiezy.rpc.DefaultFuture;
import com.xiezy.rpc.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientHandler extends SimpleChannelInboundHandler {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("收到服务器消息  " + msg.toString());
        Response response = JSONObject.parseObject(msg.toString(), Response.class);
        DefaultFuture.recive(response);
    }

}
