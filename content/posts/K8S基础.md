---
title: "K8S基础"
date: 2022-07-26T14:02:22+08:00
categories: ["K8S"]
tags: ["K8S","入门"]
draft: false
code:
  copy: true
toc:
  enable: true
---

## 一、工欲善其事

```sh
# 安装
yum install bash-completion

# 自动补全
echo 'source <(kubectl completion bash)' >>~/.bashrc
kubectl completion bash >/etc/bash_completion.d/kubectl
source /usr/share/bash-completion/bash_completion
```

```sh
# 几个重要的命令

# 创建一个资源，以 yaml 文件输出资源的描述文件，--dry-run 使当前命令并不会实际去创建资源，是 piao 资源文件 yaml 的一种方式
kubectl run my-tomcat --image=tomcat --dry-run -oyaml

# 查看 api 文档
kubectl explain <资源路径，比如：pod.spec.containers>

# debug
kubectl describe namespaces <name>

# 标签选择器
kubectl get pods -l labelKey1=labelValue1,labelKey2=labelValue2

# 字段选择器
kubectl get pods --field-selector 字段路径=字段值
```

## 二、K8S资源

> - `k8s`里面操作的资源实体，就是`k8s`的对象，可以使用`yaml`来声明对象
> - Kubernetes对象指的是Kubernetes系统的持久化实体，所有这些对象合起来，代表了集群的实际情况，**Kubernetes将应用程序的数据以Kubernetes对象的形式通过 api server存储在 etcd 中**。
> - 每一个` Kubernetes`对象都包含了两个重要的字段：`spec（目标状态）`和`status（实际状态）`，`Kubernetes`通过**对应的控制器，不断地使实际状态趋向于您期望的目标状态，实现的是最终一致性**

```sh
kubectl api-resources
```

## 三、核心文件

- `/etc/kubernetes`：以`Pod`方式安装的核心组件
- `/etc/sysconfig/kubelet`：`kubelet`额外参数配置
- `/var/lib/kubelet/config.yaml`：`kubelet`配置的位置
