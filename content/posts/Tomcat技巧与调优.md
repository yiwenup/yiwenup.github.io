---
title: "04 Tomcat技巧与调优"
date: 2023-03-20T21:27:40+08:00
categories: ["Tomcat"]
tags: ["Tomcat","应用服务器","技巧"]
draft: false
code:
  copy: true
toc:
  enable: true
---

## 一、HTTPS支持

> HTTPS 数据传输相对于 HTTP 来说会更加安全，主要区别在于：
>
> - HTTPS 协议使用时需要到电子商务授权认证机构 CA 申请 SSL（SecureSocket Layer：* TLS 是 SSL 的升级） 证书
> - 在 Tomcat 的配置中，HTTP 默认端口号是 8080，HTTPS 默认端口号是 8443
> - HTTPS 相当于 HTTP 的升级版，具有 SSL 加密的安全性传输协议，对数据的传输进行加密
> - HTTP 的连接是无状态不安全的，而 HTTPS 由 HTTP + SSL 协议构建，是可进行加密传输、身份认证的网络协议，会更安全

### 1.1 HTTPS工作原理

> HTTPS 在正常浏览器客户端和服务端数据传输之前会进行一次握手，在握手的过程中将**确定双方加密数据传输的密码信息**

![image-20240504221338914](./../images/image-20240504221338914.png)

1. 客户端向服务端提供一套能支持的加密算法
2. 服务端**选择一组加密算法**后，**将自己的身份信息以证书的形式一并返回**给客户端
3. 客户端**验证证书的合法性**，包括证书的颁发机构、当前访问地址与证书中注册的网站地址等。如果证书合法则会在浏览器上标识一个小锁的图标；如果证书不合法则提示用户当前访问不受信
4. 在证书合法或者用户接受当前访问不受信的前提下，都算验证通过。之后**客户端生成随机密码并用证书中提供的公钥加密该随机密码**
5. 客户端**使用加密过后的随机密码，对握手信息加密**后发送给服务端
6. 服务端使用私钥解密随机密码
7. 服务端**使用解密后的随机密码解密握手信息**
8. 服务端用随机密码再加密一段握手信息，连同该握手信息的Hash值一并传给客户端
9. 客户端解密握手信息并计算Hash值，如果和服务端发送来的Hash值一致则握手结束
10. **后续所有的通信数据都由之前生成的随机密码利用对称加密算法加密**

### 1.2 Tomcat配置HTTP

1. 使用 JDK 提供的工具生成免费的密钥库文件

   ```sh
   keytool -genkey -alias yiwenup -keyalg RSA -keystore yiwenup.keystore
   ```

   ![image-20240505205244542](../images/image-20240505205244542.png)

2. 配置 conf/server.xml 文件

   ```xml
   <Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol" maxThreads="150" schema="https" secure="true" SSLEnabled="true">
       <SSLHostConfig>
           <Certificate certificateKeystoreFile="/home/cert/yiwenup.keystore" certificateKeystorePassword="yiwenup" type="RSA"/>
       </SSLHostConfig>
   </Connector>
   ```

3. 使用 8443 端口访问

## 二、性能优化

> 由于 Tomcat 本身也是 Java 编写的，所以 Tomcat 的性能优化可以考虑 **JVM 方向的内存调优以及垃圾回收策略考虑**。此外还可以从 **Tomcat 本身的一些配置方面考虑**优化，比如共享线程池、IO模型等。

### 2.1 内存调优

Java 内存调优相关参数主要罗列如下：

| 参数                 | 作用                                            | 优化方向                                     |
| -------------------- | ----------------------------------------------- | -------------------------------------------- |
| -server              | 启动表示以服务端模式运行JVM                     | 开启                                         |
| -Xms                 | 最小堆内存                                      | 与-Xmx一致，避免堆内存动态调整               |
| -Xmx                 | 最大堆内存                                      | 设置为可用内存的 80%                         |
| -XX:MetaspaceSize    | 元空间初始大小                                  |                                              |
| -XX:MaxMetaspaceSize | 元空间最大大小                                  | 默认无限大，建议指定上，避免过大占用系统内存 |
| -XX:NewRatio         | 年轻代和老年代大小比值，取值为整数，默认为2     | 保持默认                                     |
| -XX:SurvivorRatio    | Eden区和Survivor区大小比值，取值为整数，默认为8 | 保持默认                                     |

通过在启动命令后，条件 JAVA_OPTS 参数配置，比如以下示例：

```bash
JAVA_OPTS="-server -Xms2048m -Xmx2048m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m"
```

可以通过 JDK 自带的内存映射工具查看内存分配情况：

```
jhsdb jmap --heap --pid <Java进程的pid>
```

### 2.2 垃圾回收策略

JDK 提供了一系列的垃圾回收策略，相关策略以及开启方式罗列如下：

| GC 策略              | 描述                                                         | 开启方式               | 备注                                                         |
| -------------------- | ------------------------------------------------------------ | ---------------------- | ------------------------------------------------------------ |
| Serial Collector     | 串行收集器。单线程执行所有垃圾回收工作，会暂停工作线程。适用于单核 CPU 服务器。 | -XX:+UseSerialGC       | 启用串行收集器                                               |
| Parallel Collector   | 并行收集器。以并行方式执行年轻代垃圾回收，但仍会暂停工作线程，只是回收开销会显著降低。适用于多核处理器或多线程硬件上运行的数据量较大的应用。 | -XX:+UseParallelGC     | 启用并行垃圾收集器。开启之后 -XX:+UseParallelOldGC 会默认启用 |
| Concurrent Collector | 并发收集器。以并发方式执行大部分回收工作，回收线程能和工作线程同时执行，但不一定是并行的，可能是交替执行方式，会对应用程序的性能有一定程度影响。适用于响应时间优先吞吐量的应用。 | +XX:UseParNewGC        | 如果设置了 -XX:UseConcMarkSweepGC 选项则会默认启用           |
| CMS Collector        | 并发标记收集器。在并发收集器上的一种算法加持。适用于愿意缩短垃圾回收暂停时间，并且能够负担起垃圾回收共享处理器资源的应用。 | -XX:UseConcMarkSweepGC | 对于老年代，启用 CMS 垃圾收集器。当并行收集器无法满足应用的延迟需求时，推荐使用 CMS 或 G1 收集器。开启之后，-XX:UseParNewGC 会默认启用并作用于年轻代 |
| G1 Collector         | JDK1.7之后的现代化垃圾回收策略，在JDK9之后是默认的垃圾回收策略。能在保持高吞吐量的情况下，高概率满足GC暂停时间的目标。适用于大容量内存的多核服务器。 | -XX:UseG1GC            | 启用 G1 收集器。                                             |

通过在启动命令后，条件 JAVA_OPTS 参数配置，比如以下示例：

```sh
JAVA_OPTS="-XX:UseConcMarkSweepGC"
```

### 2.3 Tomcat 配置

- 调整 Tomcat 线程池，配置共享线程池

- 调整 Tomcat 连接器，可以参考如下参数

  | 参数           | 说明                                                         |
  | -------------- | ------------------------------------------------------------ |
  | maxConnections | 最大连接数。当达到该值之后，服务器能接收但不会立即处理更多请求，额外的请求将会阻塞直到连接数低于 maxConnections 。可以通过 ulimit -a 查看服务器限制。对于 CPU 要求更高（计算密集型场景）时，不宜设置过大；对于 CPU 要求不是特别高时，可以视情况调整在 2000 左右，需要结合服务器硬件支持和服务器性能考虑。 |
  | maxThreads     | 最大线程数。根据服务器的硬件情况设置。                       |
  | acceptCount    | 最大排队等待数。当服务器接收的请求数量达到了 maxConnections 时，额外的请求会存在在队列中排队等待，acceptCount 就是等待的请求数，因此一个 Tomcat 的最大请求处理数量等于 maxConnections + acceptCount |

- 禁用 AJP 连接器

- 调整 I/O 模型，不过 Tomcat8.x 之后默认采用了 NIO 模型。当对并发性能有更高的要求或者出现瓶颈的时候，可以尝试开启 APR 模式，该模式要求操作系统安装 APR 和 Native 支持（因为 APR 本质上是使用 JNI 技术调用操作系统底层的 IO 接口），能够从操作系统层面解决异步 IO 问题。

- 动静分离。使用 Nginx 承担静态资源的访问处理，因为 Tomcat 相对 Nginx 来说，不擅长处理静态资源。
