package lockjk;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class DistributedLock {

    private static final Logger    logger               = LoggerFactory.getLogger(DistributedLock.class);
    private              ZooKeeper zk;
    private              String    selfPath;
    private              String    waitPath;
    private static final String    LOG_PREFIX_OF_THREAD = Thread.currentThread().getName();
    private static final String    GROUP_PATH           = "/disLocks";
    private static final String    SUB_PATH             = "/disLocks/sub";
    private              Watcher   watcher;

    public DistributedLock(ZooKeeper zk) {
        this.zk = zk;
    }

    public void setWatcher(Watcher watcher) {
        this.watcher = watcher;
    }

    /**
     * 获取锁
     *
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public boolean getLock() throws KeeperException, InterruptedException {
        selfPath = zk.create(SUB_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        logger.info(LOG_PREFIX_OF_THREAD + "创建锁路径：" + selfPath);

        return checkMinPath();
    }

    /**
     * 创建节点
     *
     * @param path 节点path
     * @param data 初始数据内容
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public boolean createPath(String path, String data) throws KeeperException, InterruptedException {
        if (zk.exists(path, false) == null) {
            logger.info(LOG_PREFIX_OF_THREAD + "节点创建成功，Path："
                    + this.zk.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
                    + ", content：" + data);
        }
        return true;
    }

    public void unlock() {
        try {
            if (zk.exists(this.selfPath, false) == null) {
                logger.error(LOG_PREFIX_OF_THREAD + "本节点已不在了。。。");
                return;
            }
            zk.delete(this.selfPath, -1);
            logger.info(LOG_PREFIX_OF_THREAD + "删除本节点：" + selfPath);
            zk.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查自己是不是最小的节点
     *
     * @return
     */
    public boolean checkMinPath() throws KeeperException, InterruptedException {
        List<String> subNodes = zk.getChildren(GROUP_PATH, false);
        Collections.sort(subNodes);
        int index = subNodes.indexOf(selfPath.substring(GROUP_PATH.length() + 1));

        switch (index) {
            case -1: {
                logger.error(LOG_PREFIX_OF_THREAD + "本节点已不在了。。。" + selfPath);
                return false;
            }
            case 0: {
                logger.info(LOG_PREFIX_OF_THREAD + "子节点中，我果然是老大" + selfPath);
                return true;
            }
            default: {
                this.waitPath = GROUP_PATH + "/" + subNodes.get(index - 1);
                logger.info(LOG_PREFIX_OF_THREAD + "获取子节点中，排在我前面的" + waitPath);
                try {
                    zk.getData(waitPath, this.watcher, new Stat());
                    return false;
                } catch (KeeperException e) {
                    if (zk.exists(waitPath, false) == null) {
                        logger.info(LOG_PREFIX_OF_THREAD + "子节点中，排在我前面的" + waitPath + "已失踪，幸福来得太突然了");
                        return checkMinPath();
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    public String getWaitPath() {
        return waitPath;
    }
}
