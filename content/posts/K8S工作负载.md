---
title: "K8S工作负载"
date: 2022-08-12T16:14:36+08:00
categories: ["K8S"]
tags: ["K8S","工作负载"]
draft: false
code:
  copy: true
toc:
  enable: true
---

## 一、啥是工作负载

```sh
# 获取 dashboard 登陆令牌
kubectl -n kubernetes-dashboard describe secret $(kubectl -n kubernetes-dashboard get secret | grep admin-user | awk '{print $1}')
```

![image-20220812163657962](../images/image-20220812163657962.png)

以上即为工作负载
