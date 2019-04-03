#### 不用zookeeper
* 先启动RmiServer,再运行RmiClient



#### 使用zookeeper

* 先启动三个Server
    1. Server1：192.168.1.102：11235
    2. Server2：192.168.1.102：11236
    3. Server3：192.168.1.102：11237
    > 在每个zookeeper下都会有三个节点，存储url地址
* 启动Client，Client先访问zookeeper，获取一个url，访问对应的Server
    > 随便停一个Server，Client还能访问其他两个Server，zookeeper的znode节点只剩两个，因为znode设置了ephemeral临时的