---
title: "一致性Hash算法"
date: 2023-10-08T17:08:39+08:00
categories: ["分布式集群解决方案"]
tags: ["解决方案","技巧","分布式系统"]
draft: false
code:
  copy: true
toc:
  enable: true
---

> 分布式一定是集群，而集群不一定是分布式的。
>
> 分布式是指将一个系统拆分为多个**子系统**，每个子系统负责各自的功能模块，独立部署各司其职
>
> 集群是指让多个实例共同工作，最常见/最简单的集群就是将一个应用复制多份部署。

## 一、什么是Hash

- 我们从一个场景展开思考：现在有一组数据（比如：1、3、2、6、5、4），需要根据给定的 n，判断这个 n 在不在这组数据中，最简单能想到的就是遍历这个数组，当匹配到 n 这个数据的时候返回出去。这便是**顺序查找法**，这种查找方式的时间复杂度为**`O(n)`**

- 基于上述算法时间复杂度过长，我们考虑采用排序搭配**折半查找法**实现，这种查找方式的时间复杂度从查询考虑的话是**`O(logn)`**

- 那么能不能更快呢？我们再思考**用一个数组 arr 作为容器存放这批数据，每个数据都存放在对应索引的位置，即 1 存放在 arr[1] 的位置**，那么查询的时候只要对应查找`arr[n]`，如果不为空则表示存在。这便是**直接寻址法**，这种查找方式的时间复杂度为**`O(1)`**

  ![image-20240508203602771](../images/image-20240508203602771.png)

- 基于<u>直接寻址法</u>的思路，我们假定这组数据数据间的差值很大（比如：1、3、2、6、5、4、10086），那么就<u>会造成过多的空间浪费</u>。为了解决这个问题，我们引入一种算法`index = n % arr.length`，即**框定一个定长的数组，用数值对数组长度取余，得到的值就是索引位置**，这便是<u>**除留余数法**，相对来说会尽可能的压缩空间</u>。

  ![image-20240508203733248](../images/image-20240508203733248.png)

- 在除留余数法的基础上，我们进一步讨论，假设数组长度取值为 5，而数据集为 [1、3、2、6、5、4、10086]，那么就会导致数据 1、6、10086 都落在索引为 1 的位置，出现**哈希冲突**。

  ![image-20240508203927324](../images/image-20240508203927324.png)

- 为解决哈希冲突问题，通常会采用**拉链法**解决，即将经过一系列计算后会出现冲突的值，用链表挂在当前索引下，但是假设在极端的条件下，所有数据都会有可能悬挂在同一索引下，还是会导致查询时间复杂度过高。

  ![image-20240508204206544](../images/image-20240508204206544.png)

  ![image-20240508204312724](../images/image-20240508204312724.png)

- 综上推导，我们知道**`Hash`是一类算法，具体算法的设计会和查询效率直接相关，越能够使数据均匀分布则查询效率越高，时间复杂度越趋近于`O(1)`**。

## 二、Hash的应用场景

> `Hash`算法总结下来可以分为两大类场景：
>
> - 请求路由的负载均衡：比如`Nginx`的`ip_hash`复杂均衡策略，还有`Hadoop`集群架构等具体场景
> - 分布式的存储解决方案：比如`Redis`集群中根据`key`的`hash`值决定存储的节点、还有`MySQL`集群架构、`ElasticSearch`集群架构等具体场景

以`Nginx`的`ip_hash`为例说明，`Nginx`的`ip_hash`策略借助`hash`实现了会话粘滞效果，即在客户端`ip`不变的情况下，总是能够将请求的路由到相同的服务实例。

![image-20240508205755141](../images/image-20240508205755141.png)

此外，在参考`Nginx`源代码中关于`ip_hash`部分的实现，可以发现他还做了一点设计，即**取客户端`ip`地址的前`3`段参与`hash`运算（hash(ip) % node_counts），意味着只要`ip`地址前`3`段相同，那么其`hash`值就是一致的，这是因为`ip`地址前`3`段相同的节点我们通常认为是在同一个局域网内的，因此这么设计的目的是为了保证能让同一局域网下的节点总是能请求到相同的一个服务实例上**。

## 三、一致性Hash算法原理

> 还是以`Nginx`为例，在普通 hash 算法的处理下，假设后端服务节点出现了变更，将会导致客户端的 hash 计算出现变更，从而使得最终路由的目标节点都会发生变更，使得原有会话产生偏移。
>
> 因此为了保证在分布式场景下，对于节点的动态扩缩容对 hash 值的计算产生影响降至最低，于是需要使用到一致性 Hash算法

- 针对一致性 Hash 算法，我们可以构想出一个**哈希环**的概念，它是一个顺时针有指向性的环状结构，长度为 2的32次方减1，大致处理逻辑如下：
  
  ![image-20240510103310152](../images/image-20240510103310152.png)
  
  1. 先对服务节点计算 hash 值，可以是按找 ip，也可以是其他方式。将其分布在哈希环之上
  2. 在对客户端路由的过程中，计算客户端的 hash 值，将其落在哈希环的某个位置上
  3. 选取哈希环顺时针方向距离客户端位置最近的一个服务端节点进行路由即可
  
- 在上述的方案中，在服务节点扩缩容的情况下，都能够避免大量路由产生迁移。尤其是**在服务节点足够多且分布足够均匀的情况下，路由的迁移成本会更低。但是与此相对的情况，当服务节点偏少（极端情况为2个节点）或者分布不均匀的时候，则很容易造成路由倾斜，导致某一个节点超负载。**

  ![image-20240510110009691](../images/image-20240510110009691.png)

- 对于上面可能出现的问题，哈希环提供了一种带**虚拟节点的哈希环**的解决方案来解决倾斜问题，大致处理逻辑如下：
  
  ![image-20240510110158372](../images/image-20240510110158372.png)
  
  1. 对服务节点取多次 hash 值（可以按不同维度：ip/uri/...），再让其分布在哈希环上
  2. 在对客户端路由的过程中，计算客户端的 hash 值，将其落在哈希环的某个位置上
  3. 选取哈希环顺时针方向距离客户端位置最近的一个服务端节点，如果当前节点是虚拟节点，则根据映射规则将其路由到真是节点

## 四、手写三类Hash算法

### 4.1 普通Hash算法示意

```java
public class GeneralHash {
    public static void main(String[] args) {
        // 假设客户端 IP 如下
        String[] clients = new String[]{"10.78.12.3", "113.25.63.4", "126.12.3.8"};

        // 假设服务端数量，编号对应（0，1，2）
        int serverCount = 3;
        for (String client : clients) {
            // hash(ip) % node_counts = index
            int hash = Math.abs(client.hashCode());
            int index = hash % serverCount;
            System.out.println("客户端" + client + "被路由到服务端" + index);
        }
    }
}
```

### 4.2 一致性Hash算法示意

```java
public class ConsistentHashWithoutVirtual {
    public static void main(String[] args) {
        // 1. 把服务节点映射到Hash环上
        String[] servers = new String[]{"192.78.12.3", "192.25.63.4", "192.12.3.5", "192.168.17.6"};
        // 选有序map，为了表达顺序性，代表hash环的顺时针指向性
        SortedMap<Integer/* hash 值*/, String/* ip 地址*/> mapping = new TreeMap<>();
        for (String server : servers) {
            int hash = Math.abs(server.hashCode());
            // 因为假设的哈希环长度就是 Integer.MAX_VALUE，所以只要计算出是整数，就可以认为在环上了
            mapping.put(hash, server);
        }

        // 2. 计算客户端的Hash值
        String[] clients = new String[]{"10.78.12.3", "113.25.63.4", "126.12.3.8"};
        for (String client : clients) {
            int hash = client.hashCode();
            // 3. 客户端按顺时针就近取服务节点
            SortedMap<Integer, String> tailMap = mapping.tailMap(hash);
            String server;
            if (tailMap.isEmpty()) {
                server = mapping.get(mapping.firstKey());
            } else {
                server = mapping.get(tailMap.firstKey());
            }
            System.out.println("客户端" + client + "被路由到服务节点" + server);
        }
    }
}
```

### 4.3 带虚拟节点的一致性Hash算法示意

```java
public class ConsistentHashWithVirtual {
    public static void main(String[] args) {
        // 1. 把服务节点映射到Hash环上
        String[] servers = new String[]{"192.78.12.3", "192.25.63.4"};

        // 针对每个真是节点虚拟出的节点格式
        int virtualCount = 3;

        // 选有序map，为了表达顺序性，代表hash环的顺时针指向性
        SortedMap<Integer/* hash 值*/, String/* ip 地址*/> mapping = new TreeMap<>();
        for (String server : servers) {
            int hash = Math.abs(server.hashCode());
            // 因为假设的哈希环长度就是 Integer.MAX_VALUE，所以只要计算出是整数，就可以认为在环上了
            mapping.put(hash, server);
            // 处理虚拟节点
            for (int i = 0; i < virtualCount; i++) {
                mapping.put(Math.abs((server + "#" + i).hashCode()), "虚拟节点" + i + "映射到：" + server);
            }
        }

        // 2. 计算客户端的Hash值
        String[] clients = new String[]{"10.78.12.3", "113.25.63.4", "126.12.3.8"};
        for (String client : clients) {
            int hash = client.hashCode();
            // 3. 客户端按顺时针就近取服务节点
            SortedMap<Integer, String> tailMap = mapping.tailMap(hash);
            String server;
            if (tailMap.isEmpty()) {
                server = mapping.get(mapping.firstKey());
            } else {
                server = mapping.get(tailMap.firstKey());
            }
            System.out.println("客户端" + client + "被路由到服务节点" + server);
        }
    }
}
```

