---
title: "failed to allocate a serviceIP: range is full"
date: 2024-06-06T10:21:08+08:00
categories: ["问题集"]
tags: ["技巧","K8S"]
draft: false
code:
  copy: true
toc:
  enable: true
---

> 公司某平台提供了应用在线发布体验能力，所有应用都可在该平台搭建的沙箱环境中直接运行，这个沙箱环境是基于`K8s`搭建的，因此**在该环境中运行的应用都会被分配一套完整的`K8s`部署资源**。

## 一、问题描述

某天上班时发现昨天有应用留言称：应用发布失败，提示`svc`创建不成功。于是放下手中早餐尝试手动为该应用先把`svc`创建起来再排查问题，当执行`kubectl apply -f app-svc.yml -n xxx`的时候，命令报错：

```bash
Internal error occurred: failed to allocate a serviceIP: range is full
```

## 二、问题分析

该报错已经非常明显了，字面意思理解应该就是：尝试为当前`svc`分配`ClusterIP`的过程中，发现在一定范围内已经没有合适的`ip`进行分配了。

**集群`IP`资源是通过`kube-apiserver`分配的，在不特殊指定下，默认是`a.b.c.d/24`，即最多只能分配`254`个网络`IP`。**在不影响整体集群的前提下，可以指定或者调整为`a.b.c.d/16`，允许分配最多`65534`个资源。

## 三、问题解决

> 公司这个`K8s`环境整体是基于二进制方式安装的

1. 通过`systemctl`找到当前`kube-apiserver`服务启动时所加载的文件

   ```bash
   systemctl status kube-apiserver
   # 对应找到 Loaded: loaded (加载的文件路径)
   ```

2. 分析该文件启动时所加载的配置，主要是`EnvironmentFile`部分对应的文件

3. 如果配置文件中有`KUBE_SERVICE_CLUSTER_IP_RANGE`部分，则调整；如果没有则添加

   ```bash
   KUBE_SERVICE_CLUSTER_IP_RANGE="--service-cluster-ip-range=a.b.c.d/16"
   ```

4. 重载配置文件并重启`kube-apiserver`服务

   ```bash
   systemctl daemon-reload && systemctl restart kube-apiserver
   ```

5. done！
