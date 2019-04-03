package demo.zookeeper.remoting.server;

import demo.zookeeper.remoting.common.Constant;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.CountDownLatch;

public class ServiceProvider {
    private static Logger logger = LoggerFactory.getLogger(ServiceProvider.class);

    /**
     * 用于等待 SyncConnected 事件触发后继续执行当前线程
     */
    private CountDownLatch latch = new CountDownLatch(1);

    /**
     * 发布 RMI 服务并注册 RMI 地址到 ZooKeeper 中
     * @param remote
     * @param host
     * @param port
     */
    public void publish(Remote remote, String host, int port) {
//        发布 RMI 服务并返回 RMI 地址
        String url = publishService(remote, host, port);
        if (url != null) {
//            连接 ZooKeeper 服务器并获取 ZooKeeper 对象
            ZooKeeper zk = connectServer();
            if (zk != null) {
//                创建 ZNode 并将 RMI 地址放入 ZNode 上
                createNode(zk, url);
            }
        }
    }

    /**
     * 发布 RMI 服务
     * @param remote
     * @param host
     * @param port
     * @return
     */
    private String publishService(Remote remote, String host, int port) {
        String url = null;

        try {
            url = String.format("rmi://%s:%d/%s", host, port, remote.getClass().getName());
            LocateRegistry.createRegistry(port);
            Naming.rebind(url, remote);
            logger.debug("publish rmi service (url : {})", url);
        } catch (Exception e) {
            logger.error("", e);
        }

        return url;
    }

    /**
     * 连接 ZooKeeper 服务器
     * @return
     */
    private ZooKeeper connectServer() {
        ZooKeeper zk = null;

        try {
            zk = new ZooKeeper(Constant.ZK_CONNECTION_STRING, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
//                        唤醒当前正在执行的线程
                        latch.countDown();
                    }
                }
            });
//            使当前线程处于等待状态
            latch.await();
        } catch (Exception e) {
            logger.error("", e);
        }

        return zk;
    }

    /**
     * 创建 ZNode节点
     * @param zk
     * @param url
     */
    private void createNode(ZooKeeper zk, String url) {
        try {
            byte[] data = url.getBytes();
//            创建一个临时性且有序的ZNode
            String path = zk.create(Constant.ZK_PROVIDER_PATH, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            logger.debug("create zookeeper node ({} => {})", path, url);
        } catch (Exception e) {
            logger.error("", e);
        }
    }
}
