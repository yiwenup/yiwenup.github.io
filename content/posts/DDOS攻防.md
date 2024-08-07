---
title: "17 DDOS攻防"
date: 2024-01-15T16:37:34+08:00
categories: ["Web安全和加速"]
tags: ["网络安全","技巧"]
draft: false
code:
  copy: true
toc:
  enable: true
---

## 一、DDOS攻防

> `DDOS（Distributed Denial of Service）`又称为分布式拒绝服务。DDOS本是利用合理的请求造成资源过载，导致服务不可用，它将正常请求放大了若干倍，通过若干个网络节点同时发起攻击，以达成规模效应。这些网络节点往往是黑客们所控制的“肉鸡”，数量达到一定规模后，就形成了一个“僵尸网络”。大型的僵尸网络，甚至达到了数万、数十万台的规模。

常见的`DDOS`攻击有`SYN flood`、`UDP flood`、`ICMP flood`等。其中`SYN flood`是一种最为经典的`DDOS`攻击，它利用了TCP协议设计中的缺陷，原理如下：

- 攻击者首先伪造地址对服务器发起`SYN`请求，服务器就会回应一个`ACK+SYN`，但是注意这是伪造的`IP`地址，实际并不会回应服务器，服务器没有收到回应，会重试`3~5`（一般是 5 次）次并且等待一个`SYNTime`（一般 30 秒 ~ 2 分钟）后，丢弃这个连接。
- 如果攻击者大量发送这种伪造源地址的`SYN`请求，由于`TCP`是可靠协议，在发现连接没有建立时就会重传报文，默认重试次数为`5`次，重试的间隔时间从`1s`开始每次都翻倍，分别为`1s + 2s + 4s + 8s +16s = 31s`，第`5`次发出后还要等`32s`才知道第`5`次也超时了，所以一共是`31 + 32 = 63s`。
- 服务器端消耗过多的资源来处理这种半连接，保存遍历会消耗非常多的`CPU`时间和内存，并且还要不断对这个列表中的`IP`进行`SYN+ACK`的重试。从而使真正的连接无法建立，无法响应正常请求，形成服务拒绝。

防护策略：买相关产品！！由于这种攻击是`TCP`层的，自建实现一边`TCP`协议成本颇高，以下仅在理论层面讨论解决方案：

- **`Cookie`源认证**：**客户端发送的`SYN`报文首先由`DDOS`防护系统来响应`syn_ack`**。防护系统响应时带上特定的 sequence number （记为cookie）。对于真实的客户端会返回一个 ack 并且 Ack number 为 cookie+1；而伪造的客户端，将不会作出响应。这样我们就可以知道那些IP对应的客户端是真实的，**将真实客户端IP加入白名单**，下次访问直接通过，而其他伪造的syn报文就被拦截
- **`Reset`认证**：**`Reset`认证利用的是`TCP`协议的可靠性，也是首先由`DDOS`防护系统来响应`syn`**。防护系统收到 syn 后响应 syn_ack ，将Ack number 设为特定值（记为cookie）。当真实客户端收到这个报文时，发现确认号不正确，将发送 reset 报文，并且sequence number 为 cookie + 1；而伪造的源，将不会有任何回应。这样我们就可以**将真实的客户端IP加入白名单**。

## 二、CC攻防

> `CC`攻击是`DDOS`攻击的一种方式，可以理解为是应用层的`DDOS`攻击，攻击者**借助代理服务器**生成指向受害主机的合法请求。

**简单的说就是针对系统某些消耗资源大的请求（大量页面资源渲染、大报文、大规模计算等），借助代理服务器不断发起调用。**

还有另一种攻击方式：就是借助一些本身流量就比较大的网站（一般是热门咨询类门户），在他们的页面植入脚本，通过`iframe`挂上要攻击的目标站点，这样也能将大部分流量灌进目标站点攻击。

防护策略：

- 应用源码设计：合理利用缓存，降低数据库压力；及时释放资源，比如数据库连接等
- 网络架构优化：集群部署采用负载均衡分摊流量；引入`CDN`和镜像站点服务缓解主站点压力
- 动静分离：将页面静态化部署，移除服务端计算节点的渲染任务负担
- 补充对抗手段：`IP`限流策略和动态黑名单机制
