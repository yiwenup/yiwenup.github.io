---
title: "03 Zookeeper基本命令"
date: 2024-03-02T20:42:00+08:00
categories: ["Zookeeper"]
tags: ["zookeeper","入门"]
draft: false
code:
  copy: true
toc:
  enable: true
---

![image-20240627175241290](../images/image-20240627175241290.png)

**创建节点**

```sh
# 临时节点无法创建子节点
create [-s] [-e] [-c] [-t ttl] path [data] [acl]
```

- -s 表示创建顺序节点
- -e 表示创建临时节点
- -c 表示创建持久节点

**查看节点**

```java
ls [-s] [-w] [-R] path
```

- -s：列出当前节点的子节点和当前节点的信息
- -w：仅列出当前节点的子节点**（会注册监听，监听是一次性的）**
- -R：列出当前节点的所有子节点（递归）

```sh
get [-s] [-w] path
```

- -s：获取当前节点的数据和节点信息
- -w：仅获取当前节点的信息**（会注册监听，监听是一次性的）**

**修改节点数据**

```sh
set [-s] [-v version] path data
```

- -s：修改当前节点数据，并展示节点信息

**删除节点**

```sh
# 无法删除有子节点的节点，如需批量删除可用 deleteall
delete [-v version] path
```

