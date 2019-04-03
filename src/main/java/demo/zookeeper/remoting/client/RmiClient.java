package demo.zookeeper.remoting.client;

import demo.zookeeper.remoting.common.HelloService;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class RmiClient {

    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {
        String       url          = "rmi://localhost:1098/demo.zookeeper.remoting.server.HelloServiceImpl";
        HelloService helloService = (HelloService) Naming.lookup(url);
        String       result       = helloService.sayHello("wangfei");

        System.out.println(result);
    }
}
