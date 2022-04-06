---
title: "module java.base does not \"opens java.lang\" to unnamed module @xxx"
date: 2022-04-03T19:12:32+08:00
categories: ["问题集"]
tags: ["问题记录","Java"]
draft: false
code:
  copy: true
toc:
  enable: true
---

## 一、问题描述

### 1. 运行环境

- Spring boot 1.5.22.RELEASE
- JDK 17
- Maven 3.8.4

### 2. 问题截图

![image-20220403230024402](../images/image-20220403230024402.png)

在运行 spring boot 项目的时候，**启动报错**，关键异常堆栈打印如上。

## 二、问题分析

如果当前服务所依赖的开发框架使用了反射技术，而 JDK 版本在 8 之后，很可能就会出现这个问题。

这是由于在 JDK 8 之后的 JDK 引入了 **Java Platform Module System** 模块，它仅允许在特定的情况下才可以使用反射，所以如果在使用反射的过程中，特别是调用了 **setAccessible** 方法，则认为是非法访问，会抛出异常，比如 Spring IOC 通过反射实例化 Bean 的时候，正是本次问题导致的原因。

![image-20220403231509675](../images/image-20220403231509675.png)

## 三、问题解决

在启动参数根据抛出的异常**决定需要开放的模块**，譬如参考本次异常抛出的是：module java.base does not "opens java.lang" to unnamed module，则在启动参数上配置`--add-opens java.base/java.lang=ALL-UNNAMED`即可

```sh
## --add-opens 报错堆栈信息对应配置=ALL-UNNAMED，常见的有以下：
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/sun.net.util=ALL-UNNAMED
```

