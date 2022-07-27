---
title: "Tomcat"
date: 2022-07-23T22:11:46+08:00
categories: ["Tomcat"]
tags: ["Tomcat","应用服务器"]
draft: true
code:
  copy: true
toc:
  enable: true
---

官网地址：https://tomcat.apache.org/

版本下载（8.5.50）：https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.50/

## Tomcat处理请求的过程

### http请求处理过程

> HTTP请求只是定义了数据的组织形式（通信格式），是一个应用层协议。数据传输依靠的是TCP/IP协议，HTTP数据内容是嵌在TCP数据包中的一段。

1. 用户在浏览器操作，发起请求。
2. 浏览器发起tcp连接请求，希望和服务器建立链接（socket）
3. 服务器接收到请求并建立链接（三次握手）
4. 连接通道建立成功之后，浏览器生成HTTP格式的数据包，发送请求数据包（请求行、请求头、请求体等）
5. 服务器解析HTTP格式数据包，并处理请求
6. 服务端发送响应数据包
7. 客户端解析HTTP格式的响应数据包并渲染页面
8. 浏览器呈现静态数据给用户

### Tomcat处理请求的过程

> Tomcat是一个HTTP服务器，能够接受并处理HTTP请求。
>
> Tomcat服务器接收到请求之后，把请求交给Servlet容器进行处理，Servlet容器通过Servlet接口调用业务类进行请求处理。注意，这里服务器并不是直接将请求交给业务类，而是中间加入了Servlet容器与Servlet接口，是为了解耦。Servlet容器和Servlet接口这一套内容称为Servlet规范。
>
> **Tomcat既按照Servlet规范提供了实现，又具备了HTTP服务器的功能。**

1. 当用户请求某个URL资源时，HTTP服务器将其原生的Request封装为一个ServletRequest对象
2. 进一步调用Servlet容器中的具体Servlet，这个过程通过URL与Servlet的映射关系进行匹配
3. 如果Servlet还没有被加载，则通过反射实例化这个Servlet，并调用他的init方法进行初始化
4. 接着调用Servlet的service方法处理请求，请求的处理结果使用ServletResponse进行封装
5. 最终将ServletResponse对象返回给HTTP服务器，HTTP服务器再把响应转化为原生的Response发送给客户端

## Tomcat总体架构

> Tomcat设计了两个主要的核心组件来完成Tomcat的两大特性：连接器（Connector）和容器（Container）
>
> - 连接器（Connector）：完成HTTP服务器特性，负责对外交流。处理Socket链接，负责网络字节流与Request和Response对象的转化。
> - 容器（Container）：完成Servlet规范，负责内部处理。加载和管理Servlet，处理具体请求。

## Tomcat连接器组件Coyote

> Coyote是Tomcat中连接组件的名称，是对外的接口。客户端通过对Coyote与服务器建立链接、发送请求并接收响应。
>
> - Coyote封装了底层的网络通信（Socket与请求、响应处理）
> - Coyote使Catalina容器与具体的请求协议以及IO操作方式完全解耦
> - Coyote使Socket输入转化为Request对象，进一步封装后，交由Catalina容器进行处理，请求处理完成之后，Catalina通过Coyote将Response对象写入输出流中
> - Coyote负责的是具体的协议（应用层）和IO（传输层）相关内容

### Coyote内部组件

- Endpoint：对传输层的抽象（TCP/IP）。是Coyote的通信端点，即通信监听的接口。具体处理Socket的通信编码，对Socket进行接收和发送。
- Processor：对应用层的抽象（HTTP/AJP）。是Coyote的协议处理接口。Processor接收到来自Endpoint的字节流后，将其解析成为Tomcat原生的Request/Response对象。
- ProtocolHandler：通过Endpoint和Processor，实现针对具体协议的处理能力。Tomcat按照支持的协议以及IO提供了6个实现：AjpNioProtpcol、AjpAprProtocol、AjpNio2Protocol、Http11NioProtocol、Http11Nio2Protocol、Http11AprProtocol。
- Adapter：将Tomcat原生的Request/Response对象转换为Servlet规范标准的ServletRequest/ServletResponse

## Tomcat容器组件Catalina

> Tomcat是由一系列可配置（conf/server.xml）的组件构成的web容器，而catalina是tomcat的servlet容器组件。
>
> 从另一个角度来说，tomcat本质上是一个servlet容器，所以可以认为catalina是tomcat的核心，其他的组件是为catalina提供支撑服务的，比如：coyote提供链接通信，jasper提供JSP引擎，Naming提供JNDI服务，Juli提供日志服务。
