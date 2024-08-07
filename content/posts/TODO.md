---
title: "TODO LIST"
date: 2021-11-10T19:12:32+08:00
categories: ["计划"]
tags: ["计划"]
draft: true
code:
  copy: true
toc:
  enable: true
---

- [x] GitHub - idea -token：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
- [x] Github - forever：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
- [x] Github - blog-token：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
- [x] Google - Zyw142701
- [ ] MongoDB
  - [ ] 内存映射
  - [ ] Page Cache
  - [ ] WiredTiger

- [x] [x-build](https://code-device.github.io/x-build)
- [x] [logo在线制作](https://www.designevo.com/)
- [ ] dapr
- [x] 腾讯云服务其：5RQJK1xj8sAxxNYN
- [x] Jakarta
- [x] springboot 2.1.7 
  - [x] 2.1.x => 2.2.x：JMX默认禁用；Spring-dependiences第三方javax扩展jar更名为Jakarta；隐藏域请求参数_method默认禁用
  - [x] 2.2.x => 2.3.x：参数校验框架不再由web-starter依赖管理，要使用须要额外引入；应用服务器线程数可配置`server.tomcat.threads`；WebServerInitializedEvent提供一种优雅停机的思路；spring-maven插件支持使用goal`build-image`，将应用打包成镜像并推送至仓库；优雅停机支持`server.shutdown=graceful``spring.lifecycle.timeout-per-shutdown-phase`；
  - [x] 2.3.x => 2.4.x：`spring.config.import`不再需要填写文件拓展名，默认是补充`.yaml`；镜像打包优化；
  - [x] 2.4.x => 2.5.x：`tomcat`的`keep-alive`支持配置文件中配置超时时间以及最大请求数量；
  - [x] 2.5.x => 2.6.x：默认禁用循环依赖；移除依赖管理`JBoss`/`Prometheus`/`Mongo`/`Oracle`等；`spring-boot-configuration-processor`支持为`lombok`的`@Value`提供元数据
- [x] maven插件/web界面的代码生成
- [ ] JVM
  - [ ] class 文件
  - [x] 双亲委派
  - [ ] GC
  - [ ] 内存模型
  - [ ] 调优
- [ ] 并发编程
  - [ ] volatile
  - [ ] synchorized
  - [ ] aqs
  - [ ] cas
  - [ ] lock
- [ ] 开发框架
  - [x] Spring
  - [x] Spring MVC
  - [x] Spring Boot
  - [x] Mybatis
  - [ ] Apache Dubbo
  - [ ] Spring Cloud Gateway
  - [ ] Spring Cloud Netflex Eureka
  - [ ] Spring Cloud Open Feign
  - [ ] Spring Cloud Alibaba Nacos
  - [ ] Spring Cloud Alibaba Sentinel
- [ ] 中间件
  - [ ] Mysql
  - [ ] Redis
  - [ ] Zookeeper
  - [ ] RabbitMQ
  - [ ] Nginx
- [ ] 设计
  - [ ] 单点登录
  - [x] 动态排序
  - [ ] DDD
  - [x] Side Car
- [ ] 数据结构与算法
  - [ ] 数据结构
    - [ ] 数组
    - [ ] 链表
    - [ ] 堆
    - [ ] 栈
    - [ ] 树
    - [ ] 图
    - [ ] 并查集
  - [ ] 算法
    - [ ] 动态规划
    - [ ] 贪心
    - [ ] 分治
    - [ ] 二分
    - [ ] 冒泡
    - [ ] 归并
    - [ ] 快排
    - [ ] 堆排序
  - [x] 问题记录
    - [x] 抽象父类动态代理

```svg
<svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" viewBox="0 0 512 512" enable-background="new 0 0 512 512" xml:space="preserve">
<linearGradient id="SVGID_1_" gradientUnits="userSpaceOnUse" x1="288.8641" y1="450.6351" x2="282.043" y2="86.8456" class="gradient-element">
	<stop offset="0" class="primary-color-gradient" style="stop-color: #8182FB"></stop>
	<stop offset="1" class="secondary-color-gradient" style="stop-color: #62E29B"></stop>
</linearGradient>
<path fill="url(#SVGID_1_)" d="M435.4,96.6L270,449.5h-66.5l-73.1-282.1l0.2-0.1c20,4.9,38.1,15,53.1,29.3l0,0c0,0,0,0.1,0,0.1
	L236,374.8c2.3,7.9,13.2,8.7,16.7,1.3l114.2-245.4c9.7-20.8,30.6-34.1,53.5-34.1H435.4z"></path>
<path d="M270,457.5h-66.5c-3.6,0-6.8-2.5-7.7-6l-71.9-277.5l-49.2-12.3c-3.6-0.9-6.1-4.1-6.1-7.8V96.6c0-4.4,3.6-8,8-8h89.1
	c29.8,0,39.8,15.7,46.3,32.6c0.1,0.4,0.3,0.8,0.3,1.2l33.4,158.1l67.2-145.8c1.9-4,6.6-5.8,10.6-3.9c4,1.9,5.8,6.6,3.9,10.6
	l-77.3,167.7c-0.2,0.5-0.5,0.9-0.8,1.3c-0.8,1.1-1.8,2-3.1,2.5c-0.6,0.3-1.2,0.5-1.8,0.6c-1.2,0.2-2.4,0.2-3.5-0.1c0,0,0,0,0,0h0
	c-1-0.2-1.9-0.6-2.7-1.2c-1.1-0.8-2-1.8-2.6-3c-0.1-0.3-0.3-0.6-0.4-0.9c-10.5-29-45.2-107.2-78.8-122.2c-4.1-1.8-9.5-3.9-14.5-5.8
	l67.7,261.1h55.3l8.4-18c1.9-4,6.6-5.7,10.6-3.8c4,1.9,5.7,6.6,3.8,10.6l-10.6,22.6C275.9,455.7,273.1,457.5,270,457.5z
	 M133.8,160.2c0.7,0.3,3.6,1.4,6.1,2.3c6.5,2.4,16.3,6,23.1,9.1c21.7,9.7,41.7,37.8,57.1,65.5l-23.4-110.8
	c-5.6-14.4-12-21.7-31.1-21.7H84.6v43.2l47.6,11.9c0.5,0.1,0.9,0.3,1.4,0.5C133.6,160.2,133.7,160.2,133.8,160.2z M296.7,400.7
	c-1.1,0-2.3-0.2-3.4-0.8c-4-1.9-5.7-6.6-3.8-10.6l73.4-156.5c1.9-4,6.6-5.7,10.6-3.8c4,1.9,5.7,6.6,3.8,10.6L303.9,396
	C302.5,398.9,299.7,400.7,296.7,400.7z M383.3,215.9c-1.1,0-2.3-0.2-3.4-0.8c-4-1.9-5.7-6.6-3.8-10.6l46.8-99.8h-77.9l-7.7,16.2
	c-1.9,4-6.7,5.7-10.7,3.8c-4-1.9-5.7-6.7-3.8-10.7l9.9-20.8c1.3-2.8,4.1-4.6,7.2-4.6h95.5c2.7,0,5.3,1.4,6.8,3.7
	c1.5,2.3,1.7,5.2,0.5,7.7l-52.1,111.2C389.2,214.1,386.3,215.9,383.3,215.9z"></path>
<path fill="#62E29B" class="secondary-color" d="M304.4,469.4v-21.8h172.3v21.8H304.4z M487.9,125.8v-101h-101L487.9,125.8z"></path>
<path d="M91.3,363.1c0,20.3-16.5,36.8-36.8,36.8c-4.4,0-8-3.6-8-8s3.6-8,8-8c11.4,0,20.8-9.3,20.8-20.8c0-4.4,3.6-8,8-8
	S91.3,358.6,91.3,363.1z M133.3,383.8c-11.4,0-20.8-9.3-20.8-20.8c0-4.4-3.6-8-8-8s-8,3.6-8,8c0,20.3,16.5,36.8,36.8,36.8
	c4.4,0,8-3.6,8-8S137.7,383.8,133.3,383.8z M133.3,405.1c-20.3,0-36.8,16.5-36.8,36.8c0,4.4,3.6,8,8,8s8-3.6,8-8
	c0-11.4,9.3-20.8,20.8-20.8c4.4,0,8-3.6,8-8S137.7,405.1,133.3,405.1z M54.5,405.1c-4.4,0-8,3.6-8,8s3.6,8,8,8
	c11.4,0,20.8,9.3,20.8,20.8c0,4.4,3.6,8,8,8s8-3.6,8-8C91.3,421.6,74.8,405.1,54.5,405.1z M448.3,228.1H335.4c-4.4,0-8,3.6-8,8
	s3.6,8,8,8h112.8c4.4,0,8-3.6,8-8S452.7,228.1,448.3,228.1z M383.3,217.1h54.1c4.4,0,8-3.6,8-8s-3.6-8-8-8h-54.1c-4.4,0-8,3.6-8,8
	S378.9,217.1,383.3,217.1z M386.4,390.9H273.6c-4.4,0-8,3.6-8,8s3.6,8,8,8h112.8c4.4,0,8-3.6,8-8S390.9,390.9,386.4,390.9z
	 M57.9,85.4V69.7h15.8c4.4,0,8-3.6,8-8s-3.6-8-8-8H57.9V37.9c0-4.4-3.6-8-8-8s-8,3.6-8,8v15.8H26.1c-4.4,0-8,3.6-8,8s3.6,8,8,8h15.8
	v15.8c0,4.4,3.6,8,8,8S57.9,89.9,57.9,85.4z M290.3,64.2h3.9v3.9c0,4.4,3.6,8,8,8c4.4,0,8-3.6,8-8v-3.9h3.9c4.4,0,8-3.6,8-8
	s-3.6-8-8-8h-3.9v-3.9c0-4.4-3.6-8-8-8c-4.4,0-8,3.6-8,8v3.9h-3.9c-4.4,0-8,3.6-8,8S285.8,64.2,290.3,64.2z"></path>
</svg>
```



