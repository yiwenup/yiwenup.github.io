---
title: "MybatisX结合IDEA代码生成"
date: 2022-06-07T19:12:32+08:00
categories: ["技巧"]
tags: ["MybatisX","工具","技巧"]
draft: false
code:
  copy: true
toc:
  enable: true
---

### 一、IDEA 连接数据库

![image-20220610181357696](../images/image-20220610181357696.png)

### 二、安装 MybatisX 插件

![image-20220610181607257](../images/image-20220610181607257.png)

### 三、逆向生成代码

1. 选中表，执行生成操作，首先配置实体类相关

   ![image-20220610181951944](../images/image-20220610181951944.png)

2. 之后配置 DAO 层 Mapper 相关

   ![image-20220610182046125](../images/image-20220610182046125.png)

### 四、 定制模版

![image-20220610182127465](../images/image-20220610182127465.png)

在 MybatisX/templates 下的就是可以自定义修改的模版了