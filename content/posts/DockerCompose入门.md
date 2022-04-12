---
title: "05_Docker Compose 入门"
date: 2022-04-12T19:12:32+08:00
categories: ["Docker Compose"]
tags: ["环境搭建","容器化"]
draft: false
code:
  copy: true
toc:
  enable: true
---

## 一、安装

> 参考官方文档：https://docs.docker.com/compose/cli-command/#install-on-linux

1. 创建安装目录

   ```bash
   DOCKER_CONFIG=${DOCKER_CONFIG:-$HOME/.docker}
   ```

   ```bash
   mkdir -p $DOCKER_CONFIG/cli-plugins
   ```

2. 下载并安装

   ```bash
   curl -SL https://github.com/docker/compose/releases/download/v2.2.3/docker-compose-linux-x86_64 -o $DOCKER_CONFIG/cli-plugins/docker-compose
   ```

3. 修改权限可执行

   ```bash
   chmod +x $DOCKER_CONFIG/cli-plugins/docker-compose
   ```

4. 测试安装是否成功

   ```bash
   docker compose version
   ```

   ![image-20220412102527805](../images/image-20220412102527805.png)