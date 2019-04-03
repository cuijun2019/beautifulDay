package demo.zookeeper.remoting.client;

import demo.zookeeper.remoting.common.HelloService;

import java.rmi.RemoteException;

public class Client {

    public static void main(String[] args) throws RemoteException, InterruptedException {
        ServiceConsumer consumer = new ServiceConsumer();

//        zookeeper测试
        while (true) {
            HelloService helloService = consumer.lookup();
            String result = helloService.sayHello("wangfei");
            System.out.println(result);
            Thread.sleep(3000);
        }
    }
}
