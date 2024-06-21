---
title: "03 Netty基本使用"
date: 2024-02-07T09:37:34+08:00
categories: ["网络编程"]
tags: ["Netty","入门"]
draft: false
code:
  copy: true
toc:
  enable: true
---

## 一、服务端编程

`Netty`服务端实现一般需要以下几个主要步骤：

1. 创建`Boss`和`Worker`线程组，根据他们的职责分工`Worker`线程组需要多分配一些线程来处理具体业务
2. 通过`ServerBoostrap`启动助手引导启动服务端：包含设置`Boss`和`Worker`线程组，以及他们的`SO_BACKLOG`和`SO_KEEPALIVE`参数设置；`NIO`通道指定；通过`ChannelInitializer`在`pipline`中注册业务处理`Handler`，此处的`Handler`一般用`Inbound`入站的就行了
3. 绑定`Server`端口，这里要注意，如果不是使用`addListener`回调方式的话，则需要使用`sync()`方法将异步转同步
4. 通过`shutdownGracefully`方法将`Boss`和`Worker`线程组释放资源

```java
public class NettyServer {
    public static void main(String[] args) {
        // 创建 Boss 线程组，由于只需要处理连接，所以线程数量可以分配少一些
        NioEventLoopGroup boosGroup = new NioEventLoopGroup(1);
        // 创建 Worker 线程组，由于实际处理业务，所以线程数量需要多分配一些，建议是 2 的次幂
        NioEventLoopGroup workGroup = new NioEventLoopGroup(2);
        // 创建服务端引导实例
        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(boosGroup, workGroup) // 分配 boss 和 worker 线程组
                .channel(NioServerSocketChannel.class) // 使用 NIO 通道
                .option(ChannelOption.SO_BACKLOG, 32) // 这里设置 Boss 线程处理连接时，等待队列的大小
                .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE) // 这里设置 worker 线程为 keepalive
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        // 将具体业务处理的 Handler 注册在 pipline 中
                        socketChannel.pipeline().addLast(new ChannelInboundHandler() {
                            @Override
                            public void channelRegistered(ChannelHandlerContext channelHandlerContext) throws Exception {

                            }

                            @Override
                            public void channelUnregistered(ChannelHandlerContext channelHandlerContext) throws Exception {

                            }

                            @Override
                            public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {

                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception {

                            }

                            @Override
                            public void channelRead(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
                                ByteBuf byteBuf = (ByteBuf) o;
                                System.out.println("服务端收到客户端消息：" + byteBuf.toString(StandardCharsets.UTF_8));
                            }

                            @Override
                            public void channelReadComplete(ChannelHandlerContext channelHandlerContext) throws Exception {
                                channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer("服务端收到...", StandardCharsets.UTF_8));
                            }

                            @Override
                            public void userEventTriggered(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {

                            }

                            @Override
                            public void channelWritabilityChanged(ChannelHandlerContext channelHandlerContext) throws Exception {

                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                                channelHandlerContext.close();
                            }

                            @Override
                            public void handlerAdded(ChannelHandlerContext channelHandlerContext) throws Exception {

                            }

                            @Override
                            public void handlerRemoved(ChannelHandlerContext channelHandlerContext) throws Exception {

                            }
                        });
                    }
                });
        // 绑定端口号
        serverBootstrap.bind(9999)
                .addListener((ChannelFutureListener) bind -> {
                    if (bind.isSuccess()) {
                        System.out.println("服务端启动成功...");
                    }
                    bind.channel().closeFuture().addListener(close -> {
                        if (close.isSuccess()) {
                            System.out.println("服务端关闭...");
                            boosGroup.shutdownGracefully();
                            workGroup.shutdownGracefully();
                        }
                    });
                });
    }
}
```

## 二、客户端编程

`Netty`客户端实现一般需要以下几个主要步骤：

1. 创建一个线程组，客户端不像服务端会承受比较大的请求压力，这里只需要使用一个线程池组就行
2. 通过`Bootstrap`启动助手引导启动服务端：包含线程组；`NIO`通道指定；通过`ChannelInitializer`在`pipline`中注册业务处理`Handler`，此处的`Handler`一般用`Inbound`入站的就行了
3. 绑定服务端的`ip`和`port`，如果不是使用`addListener`回调方式的话，则需要使用`sync()`方法将异步转同步
4. 通过`shutdownGracefully`方法将线程组释放资源

```java
public class NettyClient {
    public static void main(String[] args) {
        // 创建线程池组，用于处理事件
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);
        // 使用启动助手引导启动
        Bootstrap bootstrap = new Bootstrap()
                .group(eventLoopGroup) // 配置线程池
                .channel(NioSocketChannel.class) // 指定 NIO 通道
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        // 将具体业务处理的 Handler 注册在 pipline 中
                        socketChannel.pipeline().addLast(new ChannelInboundHandler() {
                            @Override
                            public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

                            }

                            @Override
                            public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

                            }

                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                ctx.writeAndFlush(Unpooled.copiedBuffer("这里是客户端...", StandardCharsets.UTF_8));
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {

                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf byteBuf = (ByteBuf) msg;
                                System.out.println("客户端收到服务端消息：" + byteBuf.toString(StandardCharsets.UTF_8));
                            }

                            @Override
                            public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

                            }

                            @Override
                            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

                            }

                            @Override
                            public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {

                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

                            }

                            @Override
                            public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

                            }

                            @Override
                            public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

                            }
                        });
                    }
                });
        // 指定服务端连接地址和端口号
        bootstrap.connect("127.0.0.1", 9999)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        System.out.println("客户端启动成功...");
                    }

                    future.channel().closeFuture()
                            .addListener(closeFuture -> {
                                if (closeFuture.isSuccess()) {
                                    System.out.println("客户端关闭...");
                                    eventLoopGroup.shutdownGracefully();
                                }
                            });
                });
    }
}
```

## 三、编解码器

在服务端和客户端编程的时候，我们通过`ChannelInitializer`在`pipline`中注册业务处理`Handler`，对于每一个注册的`InBoundHandler`我们都要手动将`ByteBuf`解码为字符串或者将字符串编码为`ByteBuf`。

`Netty`提供的编（解）码器实现了`ChannelHandlerAdapter`，也是一种特殊的`ChannelHandler`，说明是可以注册在`pipline`中的，而`pipline`是一条`Handler`组织成的调用链路，因此这类编解码器可以在具体逻辑执行前后总体完成编码和解码工作。

- 编码器：继承`MessageToMessageDecoder`

  ```java
  public class EncodeHandler extends MessageToMessageEncoder<String> {
      @Override
      protected void encode(ChannelHandlerContext channelHandlerContext, String s, List<Object> list) throws Exception {
          System.out.println("经过编码器....");
          list.add(Unpooled.copiedBuffer(s, StandardCharsets.UTF_8));
      }
  }
  ```

- 解码器：继承`MessageToMessageEncoder`

  ```java
  public class DecodeHandler extends MessageToMessageDecoder<ByteBuf> {
      @Override
      protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
          System.out.println("经过解码器...");
          list.add(byteBuf.toString(StandardCharsets.UTF_8));
      }
  }
  ```

- 编解码器：继承`MessageToMessageCodec`

  ```java
  public class CodecHandler extends MessageToMessageCodec<ByteBuf, String> {
      @Override
      protected void encode(ChannelHandlerContext channelHandlerContext, String s, List<Object> list) throws Exception {
          System.out.println("编码...");
          list.add(Unpooled.copiedBuffer(s, CharsetUtil.UTF_8));
      }
  
      @Override
      protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
          System.out.println("解码...");
          list.add(byteBuf.toString(CharsetUtil.UTF_8));
      }
  }
  ```


除了上述基于`Netty`提供的扩展方式编排自己的编解码逻辑之外，`Netty`本身也提供了一系列的编解码器供使用，**主要注意的事项是：编解码器在`addLast`到`pipeline`需要在实际业务处理`handle`之前，否则是不会生效的**。

- StringEncoder：字符串编码器
- StringDecoder：字符串解码器
- HttpServerCodec：用于`HTTP`请求的编解码
- ...
