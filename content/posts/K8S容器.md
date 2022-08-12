---
title: "K8S容器"
date: 2022-08-10T18:55:07+08:00
categories: ["K8S"]
tags: ["K8S", "容器"]
draft: false
code:
  copy: true
toc:
  enable: true
---

kubectl explain pod.spec.containers

<!--more-->

## 一、镜像

### 1.1 私有仓库密钥

![image-20220812143708373](../images/image-20220812143708373.png)

> 对于私有仓库，`k8s`访问获取镜像的时候需要进行密钥的配置

```sh
kubectl create secret -n dev docker-registry repo-aliyun \
  --docker-server=registry.cn-hangzhou.aliyuncs.com \
  --docker-username=市民小朱 \
  --docker-password=xxx
```

首先可以`--dry-run`看看内容

```yaml
apiVersion: v1
data:
  .dockerconfigjson: balabala
kind: Secret
metadata:
  creationTimestamp: null
  name: repo-aliyun
  namespace: dev
type: kubernetes.io/dockerconfigjson
```

其中的`.dockerconfigjson`是经过`base64`编码的，可以使用工具解码查看结构，类似如下：

```json
{
    "auths":{
        "registry.cn-hangzhou.aliyuncs.com":{
            "username":"市民小朱","password":"xxx","auth":"5biC5rCR5bCP5pyxOnoxMTAxNDA1"
        }
    }
}
```

于是，可以通过编写`deployment`文件，新增`      imagePullSecrets`字段选择使用的镜像仓库密钥

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: self-repo-test
  labels:
    app: self-repo-test
spec:
  replicas: 1
  template:
    metadata:
      name: self-repo-test
      labels:
        app: self-repo-test
    spec:
      containers:
        - name: self-repo-test
          image: registry.cn-hangzhou.aliyuncs.com/yiwenup/yiwenup-nginx:v1
          imagePullPolicy: IfNotPresent
      imagePullSecrets:
        - name: repo-aliyun
      restartPolicy: Always
  selector:
    matchLabels:
      app: self-repo-test
```

**注意：由于当前创建的密钥所属的`namespaces`是`dev`，所以在`kubectrl -f xxx.yaml`的时候也要带上`-n dev`指定命名空间，否则镜像也是拉取不到的**

### 1.2 镜像获取策略

> 默认情况是：如果显示指定了版本是`latest`，就走`Always`的策略；否则，走`IfNotPresent`策略

- Always：总是去镜像仓库获取
- IfNotPresent：如果本地不存在，则去镜像仓库获取
- Never：从不去镜像仓库拉取，坚持使用本地

## 二、启动命令

| 镜像 Entrypoint | 镜像 Cmd  | 容器 command | 容器 arg  | 命令执行       |
| --------------- | --------- | ------------ | --------- | -------------- |
| [/ep-1]         | [foo bar] | <not set>    | <not set> | [ep-1 foo bar] |
| [/ep-1]         | [foo bar] | [/ep-2]      | <not set> | [ep-2]         |
| [/ep-1]         | [foo bar] | <not set>    | [zoo boo] | [ep-1 zoo boo] |
| [/ep-1]         | [foo bar] | [/ep-2]      | [zoo boo] | [ep-2 zoo boo] |

总结：

- **一旦容器的`command`被指定了，那么最终执行的命令就不会再看镜像的配置了**
- 容器的`arg`优先于镜像的`Cmd`

## 三、环境变量

使用`env`指定，通常用于设置环境上下文信息，结合`command`和`arg`的使用如下：

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: alpine-test-env
  labels:
    app: test-env
spec:
  replicas: 1
  template:
    metadata:
      name: alpine-test-env
      labels:
        app: test-env
    spec:
      containers:
        - name: alpine-test-env
          image: alpine
          # 使用 ENV 定义变量
          env:
            - name: msg
              value: "hello world"
          # command 定义启动命令
          command:
            - "/bin/sh"
          # args 为 command 提供参数
          args:
            - "-c"
            - "echo $(msg)"
  selector:
    matchLabels:
      app: test-env
```

## 四、容器生命周期钩子

- lifecycle.postStart：在容器创建后将立刻执行。但是，并不能保证该钩子函数在容器的 `ENTRYPOINT` 之前执行。Kubernetes 在管理容器时，将一直等到 postStart 事件处理程序结束之后，才会将容器的状态标记为 Running。
- lifecycle.preStop：在容器被 terminate（终止）之前执行。如果容器已经被关闭或者进入了 `completed` 状态，preStop 钩子函数的调用将失败。该函数的执行是同步的，即，kubernetes 将在该函数完成执行之后才删除容器。该钩子函数没有输入参数。

