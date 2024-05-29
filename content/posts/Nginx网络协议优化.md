---
title: "06 Nginx网络协议优化"
date: 2024-05-10T18:50:32+08:00
categories: ["Nginx"]
tags: ["Nginx","应用服务器","技巧","解决方案"]
draft: false
code:
  copy: true
toc:
  enable: true
---

## 一、底层网络协议的影响

只要是基于`web`的网络请求，在`HTTP/1.1`协议下，都绕不开传输层`TCP`的三次握手，以及安全层`SSL/TLS`的安全通道建立。

此外，再加上客户端和服务端在网络传输上的一些本地计算，包含：证书校验、报文完整性校验等。

这些时耗是在业务逻辑之外的底层网络所产生的，不是业务逻辑优化能够解决的，所以只能从底层考虑优化方向。

## 二、针对TLS的优化

> 如果当前服务需要一些网络安全守护的话，最便捷的做法就是上`SSL/TLS`安全层，升级为`HTTPS`，这对于`Nginx`来说就是以下几行配置的事，其余的就是向`CA`申请密钥这种商业行为了。
>
> ```nginx
> server {
>     listen 443 ssl http2 default_server;
>     # listen [::]:443 ssl http2 default_server;
>     # server_name _;
>     root /usr/share/nginx/html;
>     # 配置服务证书
>     ssl_certificate "/etc/pki/nginx/server.crt";
>     # 配置服务私钥
>     ssl_certificate_key "/etc/pki/nginx/private/server.key";
> }
> ```

升级为`HTTPS`后，主要对网络有性能影响的就是`TLS`三次握手建立安全通道的过程，这个过程至少需要客户端和服务端往返两次（2*RTT，Round Trip Time），确认三个随机数用于后续通信报文的对称加密密钥生成。

优化主要是可以**开启`抢跑(False Start)`策略**，指的就是让`TLS`在完成第一次`RTT`的时候就可以直接发送应用报文了，不需要等待`TLS`整个握手完成再进行。

开启方式如下：

```nginx
server {
    listen 443 ssl http2 default_server;
    # listen [::]:443 ssl http2 default_server;
    # server_name _;
    root /usr/share/nginx/html;
    # 配置服务证书
    ssl_certificate "/etc/pki/nginx/server.crt";
    # 配置服务私钥
    ssl_certificate_key "/etc/pki/nginx/private/server.key";
    # 开启 False Start
    ssl_prefer_server_ciphers on;
    # 启用服务端算法优先，由服务器告诉客户端用哪些加密算法。当然，配置的这些算法，客户端也是需要支持的，减少一次客户端和服务端协商算法的过程
    ssl_ciphers ECDHE-RSA-AES256-SHA384:AES256-SHA256:RC4:HIGH:!aNULL:!MD5;
}
```

## 三、协议升级

默认情况下，`Nginx`应用服务器都是使用的`HTTP/1.1`协议，而`HTTP/2`相对于`HTTP/1.1`已经很成熟了，并且做了很大的性能优化，包括：头部压缩、二进制分帧、多路复用、服务端推送等。

优化主要是指定上使用的协议为`HTTP/2`即可。

配置方式如下：

```nginx
server {
    # 启用 HTTP/2 协议
    listen 443 ssl http2;
    # listen [::]:443 ssl http2 default_server;
    # server_name _;
    root /usr/share/nginx/html;
    # 配置服务证书
    ssl_certificate "/etc/pki/nginx/server.crt";
    # 配置服务私钥
    ssl_certificate_key "/etc/pki/nginx/private/server.key";
    # 开启 False Start
    ssl_prefer_server_ciphers on;
    # 启用服务端算法优先，由服务器告诉客户端用哪些加密算法。当然，配置的这些算法，客户端也是需要支持的，减少一次客户端和服务端协商算法的过程
    ssl_ciphers ECDHE-RSA-AES256-SHA384:AES256-SHA256:RC4:HIGH:!aNULL:!MD5;
}
```

## 四、 会话恢复机制选型

在`TLS/1.1`和`TLS/1.2`中，会话恢复指的是`TLS`握手建立完成后的`session`，和我们应用层开发时候所说的`session`要做区分。会话恢复用在`TLS`安全通道建立完成后，客户端或服务端突然断开连接了，后面再启动的时候，通过会话恢复机制能快速建立起安全通信，避免再来一次握手的过程。

目前，`TLS/1.1`和`TLS/1.2`支持`Session ID`和`Session Ticket`两种会话恢复机制

- `Session ID`：一次`TLS`握手的结果是建立一条对称加密的数据通道，这条数据通道相关的参数都可以在内存中保存的，所以服务端就可以针对这一套参数值生成一个`Session ID`，使用该`ID`就可以直接复原对称加密的通信通道。所以当客户端下一次请求到达的时候，客户端如果携带了`Session ID`，服务端就可以根据这个`Session ID`找到对应的`Secure Context`，从而复原信道。但是分布式服务下，**`Session ID`需要搭配一定的负载均衡策略，保证相同客户端请求能路由到同一服务端才行**，可通过以下配置开启：

  ```nginx
  server {
      # 启用 HTTP/2 协议
      listen 443 ssl http2;
      # listen [::]:443 ssl http2 default_server;
      # server_name _;
      root /usr/share/nginx/html;
      # 配置服务证书
      ssl_certificate "/etc/pki/nginx/server.crt";
      # 配置服务私钥
      ssl_certificate_key "/etc/pki/nginx/private/server.key";
      # 开启 False Start
      ssl_prefer_server_ciphers on;
      # 启用服务端算法优先，由服务器告诉客户端用哪些加密算法。当然，配置的这些算法，客户端也是需要支持的，减少一次客户端和服务端协商算法的过程
      ssl_ciphers ECDHE-RSA-AES256-SHA384:AES256-SHA256:RC4:HIGH:!aNULL:!MD5;
      # ssl_session_cache off | none | [builtin[:size]] [shared:name:size];
      # off: 使用会话缓存是严格被禁止的: nginx 明确告诉客户端会话不可以重复使用。
      # none: 使用会话缓存是温和地不允许的: nginx告诉客户端会话也许可以重用，但并不会真的在缓存中保存会话参数。
      # builtin: 内置于OpenSSL的缓存; 只被一个worker进程使用.缓存大小用会话作为单位被指明。
      # 如果缓存大小未被给出，它就等于 20480 个会话。使用内置缓存会引起内存碎片。
      # shared: 一个缓存被所有worker进程共享。缓存大小用bytes作为单位被指明。1MB可以存储大约4000个会话。
      # 每一个共享的缓存应该有一个唯一的名称。拥有相同名称的缓存可以被多个虚拟服务(virtual server)所使用。
      # 目前使用较多的配置是built-in和shared同时使用（ssl_session_cache builtin:1000 shared:SSL:10m;）
      # Nginx官方说只使用shared，性能会更高
      ssl_session_cache shared:SSL:10m;
      ssl_session_timeout 30m;
  }

- `Session Ticket`是由服务端加密提供给客户端的，在客户端存储，所以只要请求携带了这个信息，能够在服务端进行解密，那么就能快速恢复通信，是天然支持分布式的，唯一的代价就是服务端的解密开销，开启方式如下：

  ```nginx
  server {
      # 启用 HTTP/2 协议
      listen 443 ssl http2;
      # listen [::]:443 ssl http2 default_server;
      # server_name _;
      root /usr/share/nginx/html;
      # 配置服务证书
      ssl_certificate "/etc/pki/nginx/server.crt";
      # 配置服务私钥
      ssl_certificate_key "/etc/pki/nginx/private/server.key";
      # 开启 False Start
      ssl_prefer_server_ciphers on;
      # 启用服务端算法优先，由服务器告诉客户端用哪些加密算法。当然，配置的这些算法，客户端也是需要支持的，减少一次客户端和服务端协商算法的过程
      ssl_ciphers ECDHE-RSA-AES256-SHA384:AES256-SHA256:RC4:HIGH:!aNULL:!MD5;
      # 开启Session Ticket，此处没有指定加密算法，openssl默认会生成随机数的key。
      # 如果需要手动指定，配置：ssl_session_ticket_key和encode_decode.key;
      # key文件可以由openssl命令生成，例如：openssl rand 80> ticket.key
      # 如果存在Nginx集群，多个集群应用使用同一份key文件，为保证安全性须每隔一定频率更换key。
      ssl_session_tickets on;
  }
  ```

- 实际上，在`HTTP/3`的`TLS/1.3+`版本之后，`Session ID`和`Session Ticket`都会被完全取消，取而代之的是`PSK（Pre Shared Key）`。这个`PSK`并不是说每个客户端都要和服务端提前共享一个密钥，而是与握手相同的，首先使用非对称加密方法直接提前协商一个密钥出来（psk_dhe_ke ），或者直接从之前协商出来的密钥参数中得出一个密钥（psk_ke）。

## 五、开启HSTS机制

> 此前我们都是通过在`Nginx`上加一行配置，实现客户端浏览器即使访问`http://xxxx`，通过`301`跳转也能路由到`https://xxxx`上：
>
> ```nginx
> server {
>     listen 80 default_server;
>     server_name yiwenup.cloud www.yiwenup.cloud;
>     return 301 https://$server_name$request_uri;
> }
> ```

但是这种方式会导致一次客户端服务端额外通信，产生多余的`RTT`，可以开启`HSTS（HTTP Security Transport Server）`机制，强制让客户端使用`HTTPS`协议，开启方式如下：

```nginx
server {
    # 启用 HTTP/2 协议
    listen 443 ssl http2;
    server_name yiwenup.cloud;
    # listen [::]:443 ssl http2 default_server;
    # server_name _;
    root /usr/share/nginx/html;
    # 配置服务证书
    ssl_certificate "/etc/pki/nginx/server.crt";
    # 配置服务私钥
    ssl_certificate_key "/etc/pki/nginx/private/server.key";
    # 开启 False Start
    ssl_prefer_server_ciphers on;
    # 启用服务端算法优先，由服务器告诉客户端用哪些加密算法。当然，配置的这些算法，客户端也是需要支持的，减少一次客户端和服务端协商算法的过程
    ssl_ciphers ECDHE-RSA-AES256-SHA384:AES256-SHA256:RC4:HIGH:!aNULL:!MD5;
    # 开启Session Ticket，此处没有指定加密算法，openssl默认会生成随机数的key。
    # 如果需要手动指定，配置：ssl_session_ticket_key和encode_decode.key;
    # key文件可以由openssl命令生成，例如：openssl rand 80> ticket.key
    # 如果存在Nginx集群，多个集群应用使用同一份key文件，为保证安全性须每隔一定频率更换key。
    ssl_session_tickets on;
    #1. max-age：单位：秒。 HSTS header 过期时间，一般设置为1年，即31536000秒。而每次Response Header都带上HSTS Header，则可不断刷新其过期时间。
    #2. includeSubDomains：需要开启HSTS的域名/子域名。
    # 在接下来的一年（即31536000秒）中，浏览器只要向xxx或其子域名发送HTTP请求时，必须采用HTTPS来发起连接。比如，用户点击超链接或在地址栏输入 http://xxx/ ，浏览器应当自动将 http 转写成 https，然后直接向https://xxx/ 发送请求。
    add_header Strict-Transport-Security "max-age=31536000;includeSubDomains" always;
}
```

`HSTS`原理如下：

1. 第一次通过`HTTPS`请求，服务器响应`Strict-Transport-Security`头，以后尝试访问这个网站的请求都会自动把`HTTP`替换为`HTTPS`。
2. 当`HSTS`头设置的过期时间到了，后面通过`HTTP`的访问恢复到正常模式，不会再自动跳转到`HTTPS`。
3. 每次浏览器接收到`Strict-Transport-Security`头，它都会更新这个网站的过期时间，所以网站可以刷新这些信息，防止过期发生。
4. Chrome、Firefox等浏览器里，当尝试访问该域名下的内容时，会产生一个`307 Internal Redirect`（内部跳转），自动跳转到HTTPS请求。

## 六、启用OCSP Staping

> OCSP（Online Certificate Status Protocol，在线证书状态协议）具体实现是一个在线证书查询接口，它建立一个可实时响应的机制，让浏览器发送查询证书请求到CA服务器，然后CA服务器实时响应验证证书是否合法有效，这样可以实时查询每一张证书的有效性。

`OCSP`这套机制需要网络环境，因为`CA`服务通常部署在国外，按实时查询的话是会有一定的延时的。OCSP Stapling 就是为了解决 OCSP 性能问题，其工作原理如下：

1. 网站服务器将自行查询`OCSP`服务器并缓存响应结果，然后在与浏览器进行`TLS`连接时将`OCSP`查询结果通过`Certificate Status`消息发送给浏览器，这样浏览器就不需要再去查询了。
2. 浏览器客户端也不再需要向任何第三方披露用户的浏览习惯，完美解决了隐私问题。
3. 当客户端向服务器发起`SSL`握手请求时，服务器将证书的`OCSP`信息随证书链一同发送给客户端，避免了客户端验证会产生的阻塞问题。
4. 由于`OCSP`响应是无法伪造的，因此这一过程也不会产生额外的安全问题。

启用方式如下：

```nginx
server {
    # 启用 HTTP/2 协议
    listen 443 ssl http2;
    server_name yiwenup.cloud;
    # listen [::]:443 ssl http2 default_server;
    # server_name _;
    root /usr/share/nginx/html;
    # 配置服务证书
    ssl_certificate "/etc/pki/nginx/server.crt";
    # 配置服务私钥
    ssl_certificate_key "/etc/pki/nginx/private/server.key";
    # 开启 False Start
    ssl_prefer_server_ciphers on;
    # 启用服务端算法优先，由服务器告诉客户端用哪些加密算法。当然，配置的这些算法，客户端也是需要支持的，减少一次客户端和服务端协商算法的过程
    ssl_ciphers ECDHE-RSA-AES256-SHA384:AES256-SHA256:RC4:HIGH:!aNULL:!MD5;
    # 开启Session Ticket，此处没有指定加密算法，openssl默认会生成随机数的key。
    # 如果需要手动指定，配置：ssl_session_ticket_key和encode_decode.key;
    # key文件可以由openssl命令生成，例如：openssl rand 80> ticket.key
    # 如果存在Nginx集群，多个集群应用使用同一份key文件，为保证安全性须每隔一定频率更换key。
    ssl_session_tickets on;
    #1. max-age：单位：秒。 HSTS header 过期时间，一般设置为1年，即31536000秒。而每次Response Header都带上HSTS Header，则可不断刷新其过期时间。
    #2. includeSubDomains：需要开启HSTS的域名/子域名。
    # 在接下来的一年（即31536000秒）中，浏览器只要向xxx或其子域名发送HTTP请求时，必须采用HTTPS来发起连接。比如，用户点击超链接或在地址栏输入 http://xxx/ ，浏览器应当自动将 http 转写成 https，然后直接向https://xxx/ 发送请求。
    add_header Strict-Transport-Security "max-age=31536000;includeSubDomains" always;
    # 开启 OCSP Stapling ---当客户端访问时 NginX 将去指定的证书中查找 OCSP 服务的地址，获得响应内容后通过证书链下发给客户端。
    ssl_stapling on;
    # 启用OCSP响应验证，OCSP信息响应适用的证书
    ssl_stapling_verify on;
    # 指定完整的证书链
    ssl_trusted_certificate /path/chain.pem;
    # 根据Nginx文档，最好使用本地DNS服务，可以防止DNS欺骗(DNS spoofing)。使用公共的DNS服务，存在安全隐患。
    # 添加resolver解析OSCP响应服务器的主机名，valid表示缓存。
    resolver 8.8.8.8 8.8.4.4 服务器ip1 服务器ip2 valid=6000s;
    # resolver_timeout表示网络超时时间
    resolver_timeout 5s；
}
```

---

综上，比较推荐的`Nginx`网络协议侧优化方式总结如下：

```nginx
server {
    # 启用 HTTP/2 协议
    listen 443 ssl http2;
    server_name yiwenup.cloud;
    # listen [::]:443 ssl http2 default_server;
    # server_name _;
    root /usr/share/nginx/html;
    # 配置服务证书
    ssl_certificate "/etc/pki/nginx/server.crt";
    # 配置服务私钥
    ssl_certificate_key "/etc/pki/nginx/private/server.key";
    # 开启 False Start
    ssl_prefer_server_ciphers on;
    # 启用服务端算法优先，由服务器告诉客户端用哪些加密算法。当然，配置的这些算法，客户端也是需要支持的，减少一次客户端和服务端协商算法的过程
    ssl_ciphers ECDHE-RSA-AES256-SHA384:AES256-SHA256:RC4:HIGH:!aNULL:!MD5;
    # 开启Session Ticket，此处没有指定加密算法，openssl默认会生成随机数的key。
    # 如果需要手动指定，配置：ssl_session_ticket_key和encode_decode.key;
    # key文件可以由openssl命令生成，例如：openssl rand 80> ticket.key
    # 如果存在Nginx集群，多个集群应用使用同一份key文件，为保证安全性须每隔一定频率更换key。
    ssl_session_tickets on;
    #1. max-age：单位：秒。 HSTS header 过期时间，一般设置为1年，即31536000秒。而每次Response Header都带上HSTS Header，则可不断刷新其过期时间。
    #2. includeSubDomains：需要开启HSTS的域名/子域名。
    # 在接下来的一年（即31536000秒）中，浏览器只要向xxx或其子域名发送HTTP请求时，必须采用HTTPS来发起连接。比如，用户点击超链接或在地址栏输入 http://xxx/ ，浏览器应当自动将 http 转写成 https，然后直接向https://xxx/ 发送请求。
    add_header Strict-Transport-Security "max-age=31536000;includeSubDomains" always;
    # 开启 OCSP Stapling ---当客户端访问时 NginX 将去指定的证书中查找 OCSP 服务的地址，获得响应内容后通过证书链下发给客户端。
    ssl_stapling on;
    # 启用OCSP响应验证，OCSP信息响应适用的证书
    ssl_stapling_verify on;
    # 指定完整的证书链
    ssl_trusted_certificate /path/chain.pem;
    # 根据Nginx文档，最好使用本地DNS服务，可以防止DNS欺骗(DNS spoofing)。使用公共的DNS服务，存在安全隐患。
    # 添加resolver解析OSCP响应服务器的主机名，valid表示缓存。
    resolver 8.8.8.8 8.8.4.4 服务器ip1 服务器ip2 valid=6000s;
    # resolver_timeout表示网络超时时间
    resolver_timeout 5s；
}
```

