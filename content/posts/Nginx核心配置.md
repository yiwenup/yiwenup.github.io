---
title: "02 Nginx核心配置"
date: 2023-05-06T14:30:33+08:00
categories: ["Nginx"]
tags: ["Nginx","应用服务器"]
draft: false
code:
  copy: true
toc:
  enable: true
---

> Nginx 主要配置在 nginx.conf 文件中，按模块可以大致区分为：全局块、events 块以及 http 块。
>
> - 全局块：影响 Nginx 整体行为
> - events 块：影响服务端和客户端的网络通信
> - http 块：影响 HTTP 请求

## 一、完整配置概览

```nginx
# == start 全局块 ==
# 运行用户
# user nobody;

# worker进程数量，建议设置为和CPU数量相等
# worker_processes 1;

# 全局错误日志以及pid文件位置
# error_log logs/error.log
# error_log logs/error.log notice;
# error_log logs/error.log info;

# pid文件位置
# pid logs/nginx.pid
# == end 全局块 ==

# == start events事件块 ==
events {
	# 单个worker进程的最大并发连接数
	worker_connections 1024;
}
# == end envents事件块 ==

# == start http块 ==
http {
	# 引入mime类型定义文件
	include mime.types;
	default_type application/octet-stream;

	# 设置日志格式
	# log_format main '$remote_addr - $remote_user [$time_local] "$request" '
	#                 '$status $body_bytes_sent "$http_referer" '
	#                 '"$http_user_agent" "$http_x_forwarded_for"';

	# 访问日志位置
	# access_log logs/access.log main;

	# 默认情况下保持，如果是IO密集型任务，建议关闭可以提升性能
	sendfile on;

	# tcp_nopush on;

	# 连接超时时间
	keepalive_timeout 60;

	# 开启gzip压缩
	gzip on;

	# 虚拟主机块，一个http块可以有多个
	server {
		# 监听的端口号
		listen 9080;

		# 虚拟主机地址
		server_name localhost;

		# charset koi8-r;

		# access_log logs/host.access.log main;

		# 默认请求，一个虚拟主机可以配置多个请求处理规则
		location / {
			# 网站的根目录位置
			root html;

			# 欢迎页名称
			index index.html index.htm;
		}

		error_page 404 403 /40x.html
		location = /40x.html {
			root html;
		}
	}
}
```

## 二、全局块

全局块主要关注以下参数的配置：

- worker_processes：表示 worker 进程数，设置和 CPU 核数一样即可
- error_log：错误日志位置
- pid：pid文件位置

## 三、 events 块

events 块主要关注以下参数的配置：

- worker_connections：单个worker进程的最大并发连接数

## 四、http 块

> Nginx 的各种应用场景通常都表现在 http 块的配置中

http 块主要关注以下参数的配置：

- sendfile：默认情况下保持，**如果是IO密集型任务，建议关闭可以提升性能**
- keepalive_timeout：连接超时时间
- server 块：表示虚拟主机，一个 http 块可以有多个 server 块，一个 server 块可以有多个 location 块
