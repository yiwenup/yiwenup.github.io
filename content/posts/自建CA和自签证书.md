---
title: "07 自建CA和自签证书"
date: 2023-11-16T10:37:34+08:00
categories: ["Web安全和加速"]
tags: ["网络安全","技巧"]
draft: false
code:
  copy: true
toc:
  enable: true
---

> 由于向`CA`购买证书，有一定价格，并且在内网环境中一般也不是特别需要向`CA`申请证书，因此可以考虑自己搭建`CA`并通过这个`CA`来给我们的服务签发证书。

## 一、基于OpenSSL搭建CA

`OpenSSL`是一个`C`便携的实现了`SSL`和`TLS`协议的开源软件库包。`OpenSSL`主要包含三部分功能：

- 加密算法库：对称加密算法、非对称加密算法、信息摘要算法
- SSL协议库：实现了`SSLv2`和`SSLv3`，还有`TLSv1.0+`
- 命令行工具：加解密、密钥生成、密钥和证书管理、自建`CA`和签名等

1. 检查当前`openssl`版本，要求在`1.0.1`以上，因为这类版本基本都支持了`TSL`协议

   ```sh
   # 没有就安装一个
   openssl version
   ```

2. 创建初始文件，目的是为来维护后续来`CA`签发证书的序列号，每一个由`CA`签发的证书都会有一个唯一的序列号

   ```sh
   cd /etc/pki/CA
   touch index.txt serial
   echo 01 > serial
   ```

3. 为自建`CA`生成一个私钥（**当前私钥属于自建`CA`机构的**）

   ```sh
   cd /etc/pki/CA
   openssl genrsa -out private/cakey.pem 2048 # 在 /etc/pki/CA/private 目录下生成一个长度 2048 位的私钥
   ```

4. 生成自建`CA`的根证书（**当前证书属于自建`CA`机构的**）

   ```sh
   cd /etc/pki/CA
   openssl req -new -x509 -key private/cakey.pem -out cacert.pem
   ```

5. 至此，自建`CA`完成，后续可以通过这个`CA`来给某个服务签发证书

## 二、以Nginx为例签发证书

1. 使用`openssl`为`Nginx`生成私钥（**当前私钥属于应用**）

   ```sh
   cd /etc/nginx/ssl
   openssl genrsa -out nginx.key 2048
   ```

2. 为`Nginx`生成证书请求文件，后续这个文件会传给`CA`，让其签发证书

   ```sh
   cd /etc/nginx/ssl
   openssl req -new -key nginx.key -out nginx.csr
   # 后续会填写一堆信息，只要注意Common Name需要是预期的域名即可
   ```

3. 将`Nginx`服务器上的`.csr`请求文件，发送到`CA`服务器上，在`CA`服务器上执行命令生成证书（**当前证书属于应用**）

   ```sh
   openssl ca -in nginx.csr -out nginx.crt
   # 同时也可以观察到 /etc/pki/CA/index.txt 和 /etc/pki/CA/serial 两个文件产生了变化
   ```

4. 将`Nginx`证书从`CA`服务器上传回`Nginx`服务器上

5. 配置`Nginx`启用签发的证书

   ```nginx
   server {
       listen       80;
       listen  [::]:80;
       server_name  localhost;
   
       # region 加入这部分代码
       listen 443 ssl http2;
       ssl_certificate /etc/nginx/ssl/nginx.crt;
       ssl_certificate_key /etc/nginx/ssl/nginx.key;
       ssl_session_timeout 5m;
       # endregion
   
       location / {
           root   /usr/share/nginx/html;
           index  index.html index.htm;
       }
       
       error_page   500 502 503 504  /50x.html;
       location = /50x.html {
           root   /usr/share/nginx/html;
       }
   }
   ```

6. 重启`Nginx`

   ```sh
   nginx -s reload
   ```

7. 尝试访问，但是可能会出现以下报错。**原因是由于证书是自签的，而正常情况下，`CA`会把自己的证书内置在服务器或浏览器上，因此我们需将自建的`CA`证书导入到当前访问环境才行**

   ![image-20240527212121119](../images/image-20240527212121119.png)

8. 使用`ca-certificates`工具安装自建`CA`证书到当前访问环境（当前以`CentOS`服务器为例）

   ```sh
   # 安装 ca-certificates
   yum install -y ca-certificates && update-ca-trust force-enable
   # 复制CA根证书到 /etc/pki/ca-trust/source/anchors 目录下
   cd /etc/pki/ca-trust/source/anchors && cp /etc/pki/CA/cacert.pem ./
   # 更新操作系统CA证书
   update-ca-trust extract
   # 修改 /etc/hosts 域名映射，重新用访问即可
   ```
