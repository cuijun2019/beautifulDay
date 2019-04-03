package lockjk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class TestLock {

    private static final Logger         logger          = LoggerFactory.getLogger(TestLock.class);
    private static final int            THREAD_NUM      = 10;
    public static        CountDownLatch threadSemaphare = new CountDownLatch(THREAD_NUM);

    public static void main(String[] args) {
        for (int i = 0; i < THREAD_NUM; i++) {
            final int threadId = i;
            new Thread() {
                @Override
                public void run() {
                    try {
                        new LockService().doService(new DoTemplate() {
                            public void dodo() {
                                logger.info("我要修改一个文件。。。。" + threadId);
                            }
                        });
                    } catch (Exception e) {
                        logger.info("【第" + threadId + "个线程】抛出了异常");
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        try {
            Thread.sleep(60000);
            threadSemaphare.await();
            logger.info("所有线程运行结束！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
