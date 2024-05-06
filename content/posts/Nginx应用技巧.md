---
title: "04 Nginx应用技巧"
date: 2023-05-25T14:30:33+08:00
categories: ["Nginx"]
tags: ["Nginx","应用服务器","技巧"]
draft: false
code:
  copy: true
toc:
  enable: true
---

## 一、location语法

> `location`是`Nginx`使用过程中涉及配置最多的地方，一般语法如下
>
> ```nginx
> location [= | ^~ | ~* | ~] /uri/ {...}
> ```

| 匹配方式           | 示例                   | 优先级（从小到大依次升高） |
| ------------------ | ---------------------- | -------------------------- |
| 普通前缀匹配       | location /abc {...}    | 1                          |
| 一般正则匹配       | location ~ /abc {...}  | 2                          |
| 忽略大小写正则匹配 | location ~* /abc {...} | 3                          |
| 路径前缀匹配       | location ^~ /abc {...} | 4                          |
| 精确匹配           | location = /abc {...}  | 5                          |

## 二、负载均衡策略

- 轮询（默认策略），将每个请求按时间顺序逐一分配到不同的服务器，如果某个服务器下线，能够自动剔除

  ```nginx
  upstream tomcat-cluster {
    server 172.17.0.2:8080;
    server 172.17.0.4:8080;
  }
  
  server {
      location /tomcat {
          proxy_pass http://tomcat-cluster/;
      }
  }
  ```

- 权重，默认每个负载服务器的权重都是1，权重越高则被分配的请求数越多。**适用于服务器性能差异比较大的场景。**

  ```nginx
  upstream tomcat-cluster {
    server 172.17.0.2:8080 weight=1;
    server 172.17.0.4:8080 weight=2;
  }
  
  server {
      location /tomcat {
          proxy_pass http://tomcat-cluster/;
      }
  }
  ```

- IP-Hash，每个请求按照`ip`的`hash`值分配负载服务器，能保证每个客户端请求都会固定分配到同一个负载服务器上进行处理，**可以解决`session`问题**

  ```nginx
  upstream tomcat-cluster {
    ip_hash;
    server 172.17.0.2:8080 weight=1;
    server 172.17.0.4:8080 weight=2;
  }
  
  server {
      location /tomcat {
          proxy_pass http://tomcat-cluster/;
      }
  }
  ```

  
