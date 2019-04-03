package demo.zookeeper.remoting.server;

import demo.zookeeper.remoting.common.HelloService;

import java.rmi.RemoteException;

public class Server {

    public static void main(String[] args) throws RemoteException, InterruptedException {

//        当前rmi服务器的ip和端口
        String host = "192.168.1.102";
        int port = Integer.parseInt("11235");
        ServiceProvider provider = new ServiceProvider();

        HelloService helloService = new HelloServiceImpl();
        provider.publish(helloService, host, port);

        Thread.sleep(Long.MAX_VALUE);
    }
}
