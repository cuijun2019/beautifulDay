package lockjk;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class AbstractZooKeeper implements Watcher {

    protected ZooKeeper      zooKeeper;
    protected CountDownLatch countDownLatch = new CountDownLatch(1);

    public ZooKeeper connect(String host, int session_timeout) throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(host, session_timeout, this);
        countDownLatch.await();
        System.out.println("AbstractZooKeeper.connect()");
        return zooKeeper;
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getState() == Event.KeeperState.SyncConnected) {
            countDownLatch.countDown();
        }
    }

    public void close() throws InterruptedException {
        zooKeeper.close();
    }
}
