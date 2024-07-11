---
title: "04 Zookeeper客户端操作"
date: 2024-03-02T22:42:00+08:00
categories: ["Zookeeper"]
tags: ["zookeeper","入门"]
draft: false
code:
  copy: true
toc:
  enable: true
---

## 一、Apache Zookeeper

> `Apache Zookeeper`是`ZK`的原生客户端，`Maven`坐标为：**org.apache.zookeeper:zookeeper**

**创建客户端：**

```java
public class ZookeeperMain {

    private static final ZooKeeper zooKeeper;

    static {
        try {
            zooKeeper = new ZooKeeper("127.0.0.1:55003", 5000, new ZkWatcher());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            // do something...
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            zooKeeper.removeAllWatches("/", Watcher.WatcherType.Any, true);
            zooKeeper.close();
        }
    }

    static class ZkWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            switch (event.getState()) {
                case SyncConnected -> System.out.println("同步连接...");
                case Disconnected -> System.out.println("断开连接...");
                case Closed -> System.out.println("连接关闭...");
            }
        }
    }
}
```

**创建节点：**

```java
public class ZookeeperMain {

    private static final ZooKeeper zooKeeper;

    static {
        try {
            zooKeeper = new ZooKeeper("127.0.0.1:55003", 5000, new ZkWatcher());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            zooKeeper.addWatch("/a", new ZkCreatedWatcher(), AddWatchMode.PERSISTENT);
            String persistent = zooKeeper.create("/a", "test".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println("持久节点：" + persistent);
            String ps = zooKeeper.create("/a/b", "持久顺序节点".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
            System.out.println("持久顺序节点：" + ps);
            String e = zooKeeper.create("/a/c", "临时节点".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            System.out.println("临时节点：" + e);
            String es = zooKeeper.create("/a/d", "临时顺序节点".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println("临时顺序节点：" + es);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            zooKeeper.removeAllWatches("/", Watcher.WatcherType.Any, true);
            zooKeeper.close();
        }
    }
    
    static class ZkCreatedWatcher implements Watcher {
        @Override
        public void process(WatchedEvent watchedEvent) {
            if (Event.EventType.NodeChildrenChanged.equals(watchedEvent.getType())) {
                try {
                    List<String> children = zooKeeper.getChildren("/a", true);
                    System.out.println(children);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    static class ZkWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            switch (event.getState()) {
                case SyncConnected -> System.out.println("同步连接...");
                case Disconnected -> System.out.println("断开连接...");
                case Closed -> System.out.println("连接关闭...");
            }
        }
    }
}
```

**修改节点数据：**

```java
public class ZookeeperMain {

    private static final ZooKeeper zooKeeper;

    static {
        try {
            zooKeeper = new ZooKeeper("127.0.0.1:55003", 5000, new ZkWatcher());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            zooKeeper.addWatch("/a", new ZkDataChangedWatcher(), AddWatchMode.PERSISTENT);
            zooKeeper.setData("/a", "修改2".getBytes(), -1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            zooKeeper.removeAllWatches("/", Watcher.WatcherType.Any, true);
            zooKeeper.close();
        }
    }
    
    static class ZkDataChangedWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            if (Event.EventType.NodeDataChanged.equals(event.getType())) {
                try {
                    byte[] data = zooKeeper.getData("/a", false, null);
                    System.out.println("after update:" + new String(data));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    static class ZkWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            switch (event.getState()) {
                case SyncConnected -> System.out.println("同步连接...");
                case Disconnected -> System.out.println("断开连接...");
                case Closed -> System.out.println("连接关闭...");
            }
        }
    }
}
```

**删除节点：**

```java
public class ZookeeperMain {

    private static final ZooKeeper zooKeeper;

    static {
        try {
            zooKeeper = new ZooKeeper("127.0.0.1:55003", 5000, new ZkWatcher());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            zooKeeper.addWatch("/a/b0000000004", new ZkDeletedWatcher(), AddWatchMode.PERSISTENT);
            Stat exists = zooKeeper.exists("/a/b0000000004", false);
            System.out.println(exists != null ? "节点存在" : "节点不存在");
            zooKeeper.delete("/a/b0000000004", -1);
            Stat existsAfter = zooKeeper.exists("/a/b0000000004", false);
            System.out.println(existsAfter != null ? "节点存在" : "节点不存在");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            zooKeeper.removeAllWatches("/", Watcher.WatcherType.Any, true);
            zooKeeper.close();
        }
    }
    
    static class ZkDeletedWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            if (Event.EventType.NodeDeleted.equals(event.getType())) {
                try {
                    List<String> children = zooKeeper.getChildren("/a", true);
                    System.out.println(children);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    static class ZkWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            switch (event.getState()) {
                case SyncConnected -> System.out.println("同步连接...");
                case Disconnected -> System.out.println("断开连接...");
                case Closed -> System.out.println("连接关闭...");
            }
        }
    }
}
```

## 二、ZkClient

> `ZkClient`是`ZK`的开源客户端之一，`Maven`坐标为：**com.101tec:zkclient**。
>
> 在原生客户端之上，支持递归创建、删除节点，还实现了Session超时重连、Watcher反复注册等功能

**创建客户端：**

```java
public class ZkClientMain {

    private static final ZkClient ZK_CLIENT;

    static {
        ZK_CLIENT = new ZkClient("127.0.0.1:55003", 5000);
    }

    public static void main(String[] args) {
        try {
            // do something
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            ZK_CLIENT.unsubscribeAll();
            ZK_CLIENT.close();
        }
    }
}
```

- 原生`zookeeper`客户端的会话创建是异步的，`ZkClient`的实现将其同步化了

**创建节点：**

```java
public class ZkClientMain {

    private static final ZkClient ZK_CLIENT;

    static {
        ZK_CLIENT = new ZkClient("127.0.0.1:55003", 5000);
    }

    public static void main(String[] args) {
        try {
            ZK_CLIENT.subscribeChildChanges("/a", (current, children) -> System.out.println(current + " => " + children));
            ZK_CLIENT.create("/a", "aaa", CreateMode.PERSISTENT);
            TimeUnit.SECONDS.sleep(5L);
            ZK_CLIENT.createPersistent("/a/b", true);
            TimeUnit.SECONDS.sleep(5L);
            ZK_CLIENT.createPersistent("/a/d");
            TimeUnit.SECONDS.sleep(5L);
            ZK_CLIENT.createPersistentSequential("/a/c", "ac");
            TimeUnit.SECONDS.sleep(5L);
            ZK_CLIENT.createEphemeralSequential("/a/d", "11");
            TimeUnit.SECONDS.sleep(5L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            ZK_CLIENT.unsubscribeAll();
            ZK_CLIENT.close();
        }
    }
}
```

- 原生的`zookeeper`客户端无法递归创建节点，必须保证要创建的节点上一级父节点要存在；而`ZkClient`客户端是**支持了递归创建节点**的功能

**修改节点数据：**

```java
public class ZkClientMain {

    private static final ZkClient ZK_CLIENT;

    static {
        ZK_CLIENT = new ZkClient("127.0.0.1:55003", 5000);
    }

    public static void main(String[] args) {
        try {
            ZK_CLIENT.subscribeDataChanges("/a", new IZkDataListener() {
                @Override
                public void handleDataChange(String current, Object data) throws Exception {
                    System.out.println("节点" + current + "数据已修改为：" + data);
                }

                @Override
                public void handleDataDeleted(String current) throws Exception {
                    System.out.println("节点" + current + "数据被删除...");
                }
            });
            String before = ZK_CLIENT.readData("/a", true);
            System.out.println("修改前：" + before);
            TimeUnit.SECONDS.sleep(3L);
            ZK_CLIENT.writeData("/a", "修改了");
            TimeUnit.SECONDS.sleep(3L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            ZK_CLIENT.unsubscribeAll();
            ZK_CLIENT.close();
        }
    }
}
```

**删除节点：**

```java
public class ZkClientMain {

    private static final ZkClient ZK_CLIENT;

    static {
        ZK_CLIENT = new ZkClient("127.0.0.1:55003", 5000);
    }

    public static void main(String[] args) {
        try {
            ZK_CLIENT.subscribeDataChanges("/a/d", new IZkDataListener() {
                @Override
                public void handleDataChange(String current, Object data) throws Exception {
                    System.out.println("节点" + current + "数据已修改为：" + data);
                }

                @Override
                public void handleDataDeleted(String current) throws Exception {
                    System.out.println("节点" + current + "数据被删除...");
                }
            });
            boolean before = ZK_CLIENT.exists("/a/d");
            TimeUnit.SECONDS.sleep(3L);
            System.out.println(before ? "节点存在" : "节点不存在");
            ZK_CLIENT.delete("/a/d");
            TimeUnit.SECONDS.sleep(3L);
            boolean after = ZK_CLIENT.exists("/a/d");
            TimeUnit.SECONDS.sleep(3L);
            System.out.println(after ? "节点存在" : "节点不存在");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            ZK_CLIENT.unsubscribeAll();
            ZK_CLIENT.close();
        }
    }
}
```

**递归删除节点：**

```java
public class ZkClientMain {

    private static final ZkClient ZK_CLIENT;

    static {
        ZK_CLIENT = new ZkClient("127.0.0.1:55003", 5000);
    }

    public static void main(String[] args) {
        try {
            ZK_CLIENT.subscribeDataChanges("/a", new IZkDataListener() {
                @Override
                public void handleDataChange(String current, Object data) throws Exception {
                    System.out.println("节点" + current + "数据已修改为：" + data);
                }

                @Override
                public void handleDataDeleted(String current) throws Exception {
                    System.out.println("节点" + current + "数据被删除...");
                }
            });
            boolean before = ZK_CLIENT.exists("/a");
            TimeUnit.SECONDS.sleep(3L);
            System.out.println(before ? "节点存在" : "节点不存在");
            ZK_CLIENT.deleteRecursive("/a");
            TimeUnit.SECONDS.sleep(3L);
            boolean after = ZK_CLIENT.exists("/a");
            TimeUnit.SECONDS.sleep(3L);
            System.out.println(after ? "节点存在" : "节点不存在");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            ZK_CLIENT.unsubscribeAll();
            ZK_CLIENT.close();
        }
    }
}
```

- `ZkClient`**支持递归删除节点功能**

## 三、Curator

> `Curator`是`ZK`的开源客户端之一，`Maven`坐标为：**org.apache.curator:curator-framework**
>
> `Curator`区别于其他客户端最主要的特性就是`Fluent`编程风格；另外还有命名空间概念，有助于实现业务数据的区分

**创建客户端：**

```java
public class CuratorMain {

    private final static CuratorFramework CLIENT;

    static {
        CLIENT = CuratorFrameworkFactory
                .builder()
                .connectString("127.0.0.1:55003")
                .retryPolicy(new BoundedExponentialBackoffRetry(3000, 5000, 3))
                .namespace("test")
                .build();
        CLIENT.start();
    }

    public static void main(String[] args) {
        try {
            // do something
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CLIENT.watchers().removeAll();
            CLIENT.close();
        }
    }
}
```

- `Curator`支持以建造者模式创建客户端，并且需要调用`start()`方法触发客户端连接
- connectString：zk的server地址，**多个server之间使⽤英⽂逗号分隔开**
- connectionTimeoutMs：连接超时时间，默认是15s
- sessionTimeoutMs：会话超时时间，默认是60s
- retryPolicy（ExponentialBackoffRetry）：失败重试策略
  - baseSleepTimeMs：初始的sleep时间，⽤于计算之后的每次重试的sleep时间，计算公式：当前sleep时间=baseSleepTimeMs*Math.max(1,random.nextInt(1<<(retryCount+1)))
  - maxSleepMs：最⼤sleep时间，如果上述的当前sleep计算出来⽐这个⼤，那么sleep⽤这个时间，默认的最⼤时间是Integer.MAX_VALUE毫秒
  - maxRetries：最⼤重试次数

**新增节点：**

```java
public class CuratorMain {

    private final static CuratorFramework CLIENT;

    static {
        CLIENT = CuratorFrameworkFactory
                .builder()
                .connectString("127.0.0.1:55003")
                .retryPolicy(new BoundedExponentialBackoffRetry(3000, 5000, 3))
                .namespace("test")
                .build();
        CLIENT.start();
    }

    public static void main(String[] args) {
        try {
            String node1 = CLIENT.create().forPath("/a");
            System.out.println("节点" + node1 + "创建成功");
            String node2 = CLIENT.create().creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath("/a/b", "bbbb".getBytes(StandardCharsets.UTF_8));
            System.out.println("节点" + node2 + "创建成功");
            String node3 = CLIENT.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath("/b/c", "ccc".getBytes(StandardCharsets.UTF_8));
            System.out.println("节点" + node3 + "创建成功");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CLIENT.watchers().removeAll();
            CLIENT.close();
        }
    }
}
```

- `Curator`支持通过`creatingParentsIfNeeded`方法，实现节点的递归创建，对于父节点是否存在是可以动态判断的

**修改节点数据：**

```java
public class CuratorMain {

    private final static CuratorFramework CLIENT;

    static {
        CLIENT = CuratorFrameworkFactory
                .builder()
                .connectString("127.0.0.1:55003")
                .retryPolicy(new BoundedExponentialBackoffRetry(3000, 5000, 3))
                .namespace("test")
                .build();
        CLIENT.start();
    }

    public static void main(String[] args) {
        try {
            String service = CLIENT.create().creatingParentContainersIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath("/service/user", "UserService#queryUser".getBytes(StandardCharsets.UTF_8));
            System.out.println("节点" + service + "已创建");
            System.out.println("[before] 节点" + service + "数据：" + new String(CLIENT.getData().forPath("/service/user")));
            CLIENT.setData().forPath("/service/user", "UserService#updateUser".getBytes(StandardCharsets.UTF_8));
            System.out.println("[after] 节点" + service + "数据：" + new String(CLIENT.getData().forPath("/service/user")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CLIENT.watchers().removeAll();
            CLIENT.close();
        }
    }
}
```

- 如果在`.withVersion()`指定了删除节点的版本，当版本不存在的时候是会抛出异常的

**删除节点：**

```java
public class CuratorMain {

    private final static CuratorFramework CLIENT;

    static {
        CLIENT = CuratorFrameworkFactory
                .builder()
                .connectString("127.0.0.1:55003")
                .retryPolicy(new BoundedExponentialBackoffRetry(3000, 5000, 3))
                .namespace("test")
                .build();
        CLIENT.start();
    }

    public static void main(String[] args) {
        try {
            Stat before = CLIENT.checkExists().forPath("/service/user");
            System.out.println("节点：" + (before == null ? "不存在" : "存在"));
            CLIENT.delete().forPath("/service/user");
            Stat after = CLIENT.checkExists().forPath("/service/user");
            System.out.println("节点：" + (after == null ? "不存在" : "存在"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CLIENT.watchers().removeAll();
            CLIENT.close();
        }
    }
}
```

- 如果在`.withVersion()`指定了删除节点的版本，当版本不存在的时候是会抛出异常的
- 可以使用`guaranteed()`实现节点的强制删除，只要会话保持住就可以持续尝试删除这个节点

