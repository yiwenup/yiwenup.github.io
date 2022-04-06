---
title: "03_Docker 安装 Redis"
date: 2021-11-12T19:12:32+08:00
categories: ["Docker"]
tags: ["环境搭建","容器化","Redis"]
draft: false
code:
  copy: true
toc:
  enable: true
---

1. 获取镜像

   ```sh
   docker pull redis:6.2.6
   ```

   ```sh
   docker images
   ```

2. 创建本地挂载目录

   ```sh
   mkdir -p ~/docker/redis
   ```

3. 预创建本地映射的`redis.conf`文件，否则待容器启动后，`redis.conf`将被误处理为文件夹

   ```sh
   mkdir -p ~/docker/redis/conf/
   ```

   ```sh
   cd ~/docker/redis/conf/
   ```

   ```sh
   touch redis.conf
   ```

4. 创建容器

   ```sh
   docker run -p 6379:6379 --name redis \
   -v ~/docker/redis/data:/data \
   -v ~/docker/redis/conf/redis.conf:/etc/redis/redis.conf \
   --restart=always \
   --privileged=true \
   -d redis:6.2.6 redis-server /etc/redis/redis.conf
   ```

5. 修改`redis.conf`

   > 全量配置项见官网：https://raw.githubusercontent.com/redis/redis/6.2/redis.conf

   - requirepass：Redis 访问密码
   - appendonly：AOF 持久化
   - bind：修改为`bind 0.0.0.0`，否则远程无法访问

6. 重启容器，查看是否重启成功

   ```sh
   docker restart redis
   ```

   ```sh
   docker ps
   ```

   