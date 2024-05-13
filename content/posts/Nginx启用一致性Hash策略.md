---
title: "06 Nginx启用一致性Hash策略"
date: 2024-05-10T13:50:32+08:00
categories: ["Nginx"]
tags: ["Nginx","应用服务器","技巧","解决方案"]
draft: false
code:
  copy: true
toc:
  enable: true
---

## 一、原生安装

> 首先要保证能以原生方式`make install`过了`Nginx`并且能够正常启动

1. 下载个人开发的`Nginx`扩展模块`https://github.com/replay/ngx_http_consistent_hash`，目前源代码托管在`Github`上

2. 解压下载的`zip`包

3. 进入`nginx`源码目录，执行以下命令安装扩展模块

   ```sh
   ./configure --add-module=<一致性Hash扩展模块本地文件夹>
   ```

4. 重新编译并安装

   ```sh
   make && make install
   ```

5. 修改`nginx.config`配置文件，并`reload`即可

   ```nginx
   upstream tomcat-server {
       # 之前普通Hash的配置方式为：ip_hash
       # 一致性Hash策略配置
       consistent_hash $request_uri; # 支持的配置为 $remote_addr/$request_uri/$args
       server 192.168.72.34:8080;
       server 192.168.23.456:8080;
   }
   ```

## 二、容器化部署

TODO
