package com.xiezy;

import com.alibaba.fastjson.JSONObject;
import com.xiezy.medium.BeanMethod;
import com.xiezy.medium.Media;
import com.xiezy.rpc.Request;
import com.xiezy.rpc.Response;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ChannelHandler.Sharable
public class NettyHandler extends SimpleChannelInboundHandler {

    @Resource(name = "zkClient")
    private CuratorFramework zkClient;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("收到客户端信息: " + msg.toString());
        Request request = JSONObject.parseObject(msg.toString(), Request.class);

        Response response = new Response();
        response.setId(request.getId());

        String method = request.getMethod();
        BeanMethod beanMethod = Media.beanMethodMap.get(method);
        if (null == beanMethod) {
            response.setContext("请求方法不存在!!");
        } else {
            Object[] args = request.getArgs();
            String[] strArgs = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                strArgs[i] = JSONObject.toJSONString(args[i]);
            }
            beanMethod.setArgs(strArgs);
            response.setContext(beanMethod.process());
        }

        ctx.channel().writeAndFlush(JSONObject.toJSONString(response));
        ctx.channel().writeAndFlush("\r\n");
    }
}
