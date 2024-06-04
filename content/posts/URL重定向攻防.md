---
title: "11 URL重定向攻防"
date: 2024-01-10T12:37:34+08:00
categories: ["Web安全和加速"]
tags: ["网络安全","技巧"]
draft: false
code:
  copy: true
toc:
  enable: true
---

> **重定向一般是用于钓鱼攻击的，攻击者利用网站的链接跳转特性，在重定向地址后拼接钓鱼网站的地址，诱骗用户重定向到钓鱼网站中。**
>
> 比如网站很常见都会做一些用户体验优化：用户点击添加购物车后，跳转到登陆页面引导用户登陆成功后，再跳转回商品列表页面；用户分享文章给好友，之后再跳转回文章浏览页面等。
>
> **这类场景都有一个特点：需要引导用户去某个页面操作，在用户完成操作之后再跳转回来继续处理。这类场景都是重定向钓鱼的攻击对象。**

## 一、攻击步骤

1. 攻击者获取到网站跳转地址，比如`http://www.normal.com/article/99999999?redirect=http://www.share.normal.com/xxx`

2. 攻击者篡改跳转地址，比如`http://www.normal.com/article/99999999?redirect=http://www.hacker.com`

3. 攻击者诱导用户点击篡改后的跳转链接，直接引导用户跳转到钓鱼网站造成损失

## 二、防护策略

> 这类攻击主要是需要服务端做好跳转的目标地址的校验。一方面，因为跳转的 key 是很容易被猜测的：`redirect`、`redirect_url`、`target`等，另一方面，根据关键词检测跳转目标地址也是不可取的，绕过方式很多：`http://www.normal.com/article?redirect=http://www.hacker.com/www.normal.com`或者`http://www.normal.com/article?redirect=http://www.hacker.com#www.normal.com`等

1. 代码固定跳转地址，不让用户控制变量

2. 跳转目标地址采用白名单映射机制。比如`1`代表`share.normal.com`，`2`代表`buy.normal.com`，其它记录日志

3. 合理充分的校验机制，校验跳转的目标地址，非己方地址时告知用户跳转风险，像知乎这些网站，跳转前会有风险提示给用户
