package com.xiezy.watcher;

import com.xiezy.netty.NettyClient;
import com.xiezy.util.SpringUtil;
import io.netty.channel.ChannelFuture;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZKServerListWatcher implements CuratorWatcher {
    @Override
    public void process(WatchedEvent event) {
        System.out.println("watch===============================");
        CuratorFramework zkClient = SpringUtil.getBean(CuratorFramework.class);
        //获取变化的路径
        String path = event.getPath();

        Map<String, ChannelFuture> oldServerChannel = NettyClient.keepSockets;
        Map<String, ChannelFuture> newServerChannel = new ConcurrentHashMap<String, ChannelFuture>();
        try {
            //重新添加监听
            zkClient.getChildren().usingWatcher(this).forPath(path);

            List<String> serverPaths = zkClient.getChildren().forPath(path);
            NettyClient.realServerPaths = new String[serverPaths.size()];
            for (int i = 0; i < serverPaths.size(); i++) {
                String serverPath = serverPaths.get(i).split("#")[0];
                NettyClient.realServerPaths[i] = serverPath;

                if (oldServerChannel.containsKey(serverPath)) {
                    newServerChannel.put(serverPath, oldServerChannel.get(serverPath));
                }
            }

            NettyClient.keepSockets = newServerChannel;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
