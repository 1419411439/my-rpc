package com.xiezy.netty;

import com.alibaba.fastjson.JSONObject;
import com.xiezy.constant.ZKConstant;
import com.xiezy.rpc.DefaultFuture;
import com.xiezy.rpc.Request;
import com.xiezy.rpc.Response;
import com.xiezy.util.SpringUtil;
import com.xiezy.watcher.ZKServerListWatcher;
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
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NettyClient {

    //实现轮询负载均衡
    private static AtomicInteger nextServerCyclicCounter = new AtomicInteger(0);

    public static Map<String, ChannelFuture> keepSockets = new ConcurrentHashMap<String, ChannelFuture>();

    public static String[] realServerPaths = null;

    //netty
    private static EventLoopGroup group = null;
    private static Bootstrap bootstrap = null;
    private static ChannelFuture connect = null;

    static {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
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

        CuratorFramework zkClient = SpringUtil.getBean(CuratorFramework.class);

        //获取zk服务ip地址，并根据轮询算法使用服务
        List<String> serverPaths = null;
        try {
            //添加监听器监控服务列表变化
            CuratorWatcher zkServerListWatcher = new ZKServerListWatcher();
            zkClient.getChildren().usingWatcher(zkServerListWatcher).forPath(ZKConstant.SERVER_PATH);

            //保存服务ip列表
            serverPaths = zkClient.getChildren().forPath(ZKConstant.SERVER_PATH);
            realServerPaths = new String[serverPaths.size()];
            for (int i = 0; i < serverPaths.size(); i++) {
                realServerPaths[i] = serverPaths.get(i).split("#")[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String getServerPath() {
        if (null == realServerPaths || realServerPaths.length <= 0) {
            return "";
        }
        int size = realServerPaths.length;
        int next = incrementAndGetModulo(size);
        String serverPath = realServerPaths[next];

        return serverPath;
    }

    public static Response send(Request request) {
        String serverPath = getServerPath();
        Response response = null;
        if (!StringUtils.isEmpty(serverPath)) {
            if (null == keepSockets.get(serverPath)) {
                connect = bootstrap.connect(serverPath, 9999);
                keepSockets.put(serverPath, connect);
            } else {
                connect = keepSockets.get(serverPath);
            }
            connect.channel().writeAndFlush(JSONObject.toJSONString(request));
            connect.channel().writeAndFlush("\r\n");

            DefaultFuture reqFuture = new DefaultFuture(request);

            response = reqFuture.getTimeout(null);
        } else {
            response = new Response();
            response.setId(-1L);
            response.setContext("无可用服务");
        }

        return response;
    }

    private static int incrementAndGetModulo(int modulo) {
        int current;
        int next;
        do {
            current = nextServerCyclicCounter.get();
            next = (current + 1) % modulo;
        } while(!nextServerCyclicCounter.compareAndSet(current, next));

        return next;
    }
}
