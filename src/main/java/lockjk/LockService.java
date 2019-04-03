package lockjk;

import org.apache.zookeeper.ZooKeeper;

public class LockService {

    //    确保所有线程运行结束
    private static final String CONNECTION_STRING = "192.168.254.11:2181,192.168.254.12:2181,192.168.254.13:2181";
    private static final String GROUP_PATH        = "/disLocks";
    private static final int    SESSION_TIMEOUT   = 10000;
    AbstractZooKeeper az = new AbstractZooKeeper();

    public void doService(DoTemplate doTemplate) {
        try {
            ZooKeeper zk = az.connect(CONNECTION_STRING, SESSION_TIMEOUT);
            DistributedLock dc = new DistributedLock(zk);
            LockWatcher lw = new LockWatcher(dc, doTemplate);
            dc.setWatcher(lw);
//            GROUP_PATH不存在的话，由一个线程创建即可
            dc.createPath(GROUP_PATH, "该节点由线程" + Thread.currentThread().getName() + "创建");
            boolean rs = dc.getLock();
            if (rs) {
                lw.dosomething();
                dc.unlock();
            }
        } catch (Exception e) {

        }
    }
}
