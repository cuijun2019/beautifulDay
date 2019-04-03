package demo.zookeeper.remoting.client;

import demo.zookeeper.remoting.common.Constant;
import demo.zookeeper.remoting.server.ServiceProvider;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Remote;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class ServiceConsumer {
    private static Logger logger = LoggerFactory.getLogger(ServiceProvider.class);

    /**
     * 用于等待 SyncConnected 事件触发后继续执行当前线程
     */
    private CountDownLatch latch = new CountDownLatch(1);

    /**
     * 定义一个volatile 成员变量，用于保存最新的 RMI 地址（考虑到该变量或许会被其他线程修改，一旦修改后，该变量的值会影响到）
     * volatile让变量每次在使用的时候，都从主存中取。而不是从各个线程的“工作内存”。
     * volatile具有synchronized关键字的“可见性”，但是没有synchronized关键字的"并发正确性"，也就是说不保证线程执行的有序
     * 也就是说，volatile变量对于每次使用，线程都能得到当前volatile变量的最新值。但是volatile变量并不保证并发的正确性。
     */
    private volatile List<String> urlList = new ArrayList<String>();

    /**
     * 构造器
     */
    public ServiceConsumer() {
//        连接 ZooKeeper 服务器并获取 ZooKeeper 对象
        ZooKeeper zk = connectServer();
        if (zk != null) {
//            观察 /registry 节点的所有子节点并更新 urlList 成员变量
            watchNode(zk);
        }
    }

    /**
     * 查找 RMI 服务
     * @param <T>
     * @return
     */
    public <T extends Remote> T lookup() {
        T service = null;
        int size =urlList.size();
        if (size > 0) {
            String url;
            if (size == 1) {
//                若 urlList 中只有一个元素，则直接获取该元素
                url = urlList.get(0);
                logger.debug("using only url: {}", url);
                System.out.println(url);
            } else {
//                若 urlList 中
                url = urlList.get(new Random().nextInt(size));
                logger.debug("using random url: {}", url);
                System.out.println(url);
            }
//            从 JNDI 中查找 RMI 服务
            service = lookupService(url);
        }
        return service;
    }

    private <T extends Remote> T lookupService(String url) {
        return null;
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
     * 观察 /registry 节点下所有子节点是否有变化
     * @param zk
     */
    private void watchNode(final ZooKeeper zk) {
        try {
            List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
//                        若子节点有变化，则重新调用该方法（为了获取最新子节点中的数据）
                        watchNode(zk);
                    }
                }
            });
//            用于存放 /registry 所有子节点中的数据
            List<String> dataList = new ArrayList<String>();
            for (String node : nodeList) {
                byte[] data = zk.getData(Constant.ZK_REGISTRY_PATH + "/" + node, false, null);
                dataList.add(new String(data));
            }
            logger.debug("node data: {}", dataList);
//            更新最新的 RMI 地址
            urlList = dataList;
        } catch (Exception e) {
            logger.error("", e);
        }
    }
}
