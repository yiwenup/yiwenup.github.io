---
title: "基于虚拟机搭建K8S集群（KubeAdm）"
date: 2021-11-10T19:12:32+08:00
categories: ["K8S"]
tags: ["K8S","云原生","PD17"]
draft: false
code:
  copy: true
toc:
  enable: true
---

## 一、虚拟机应用选型

> 市面上存在许多优秀的虚拟机产品，比如`VMware`、`Parallels`、`VirtualBox`等。
>
> 由于本人操作系统是`MacOS`，且是基于`arm`架构的`m1`芯片，所以可选条件显而易见，目前能够很好的支持`m1`的虚拟机只有`Parallels Desktop`了，故而笔者接下来使用的是`Parallels Desktop17`。

## 二、操作系统选型

![image-20220721135506725](../images/image-20220721135506725.png)

> 本人惯用的`linux`发行版本是`CentOS7`，但是在`m1`的`Parallels`中安装经历了不少波折，大概的原因是`arm`架构的`CentOS7`内核系统页参数和宿主机的不兼容所致。加之`CentOS7`已经宣布停止维护了。
>
> 所以基于以下需求：去图形化界面、操作系统最接近`CentOS7`、镜像是`arm`架构版本的。最终选择了`Rocky Linux 9.0`。

## 三、虚拟机安装操作系统

> 虚拟安装跟着向导即可，只是`Parallels Desktop`是需要付费的，小伙伴们可以自行想办法🤫
>
> `Rocky Linux`操作系统的安装也是有图形化界面指引的

## 四、系统环境预设置

### 4.1 集群建议

1. 使用`PD`准备三台`Linux`，每台操作系统的配置建议至少是 **2C4G**
2. 正常情况下，我们基于`PD`准备的三台机器应该都是可以互相`ping`通的

### 4.2 关闭防火墙

```sh
# 禁用防火墙
systemctl stop firewalld
systemctl disable firewalld
```

```sh
# 禁用安全策略
sed -i 's/enforcing/disabled/' /etc/selinux/config
setenforce 0
```

### 4.3 修改主机名称

```sh
# 设置主机名
hostnamectl set-hostname <主机名称：建议k8s-master/k8s-worker1/k8s-worker2>
# 查看主机名
hostnamectl
```

```sh
# 设置主机名解析
echo "127.0.0.1   $(hostname)" >> /etc/hosts
```

```sh
# 这里建议最好再修改一遍 /etc/hosts 文件
vim /etc/hosts
# 追加其余两台机器的 ip 和主机名的映射
```

### 4.4 关闭内存交换

```sh
swapoff -a  
sed -ri 's/.*swap.*/#&/' /etc/fstab
```

### 4.5 设置流量桥接

```sh
## 开启br_netfilter
modprobe br_netfilter
## 确认修改
lsmod | grep br_netfilter
```

```sh
# 将桥接的 IPv4 流量传递到 iptables 的链：
# 修改 /etc/sysctl.conf
# 如果有配置，则修改
sed -i "s#^net.ipv4.ip_forward.*#net.ipv4.ip_forward=1#g"  /etc/sysctl.conf
sed -i "s#^net.bridge.bridge-nf-call-ip6tables.*#net.bridge.bridge-nf-call-ip6tables=1#g"  /etc/sysctl.conf
sed -i "s#^net.bridge.bridge-nf-call-iptables.*#net.bridge.bridge-nf-call-iptables=1#g"  /etc/sysctl.conf
sed -i "s#^net.ipv6.conf.all.disable_ipv6.*#net.ipv6.conf.all.disable_ipv6=1#g"  /etc/sysctl.conf
sed -i "s#^net.ipv6.conf.default.disable_ipv6.*#net.ipv6.conf.default.disable_ipv6=1#g"  /etc/sysctl.conf
sed -i "s#^net.ipv6.conf.lo.disable_ipv6.*#net.ipv6.conf.lo.disable_ipv6=1#g"  /etc/sysctl.conf
sed -i "s#^net.ipv6.conf.all.forwarding.*#net.ipv6.conf.all.forwarding=1#g"  /etc/sysctl.conf
# 可能没有，没有则追加
echo "net.ipv4.ip_forward = 1" >> /etc/sysctl.conf
echo "net.bridge.bridge-nf-call-ip6tables = 1" >> /etc/sysctl.conf
echo "net.bridge.bridge-nf-call-iptables = 1" >> /etc/sysctl.conf
echo "net.ipv6.conf.all.disable_ipv6 = 1" >> /etc/sysctl.conf
echo "net.ipv6.conf.default.disable_ipv6 = 1" >> /etc/sysctl.conf
echo "net.ipv6.conf.lo.disable_ipv6 = 1" >> /etc/sysctl.conf
echo "net.ipv6.conf.all.forwarding = 1"  >> /etc/sysctl.conf
# 执行命令以应用
sysctl -p
```

### 4.6 预装容器运行时

> 本次采用`Docker`，选择的版本是`20.10.15`

```sh
# 卸载旧版本的 docker
sudo yum remove docker*

# 配置 docker yum 源
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo

# 查询可用的 docker 版本
yum list docker-ce --showduplicates | sort -r

# 安装 docker 
yum install -y docker-ce-20.10.15 docker-ce-cli-20.10.15 containerd.io

# 启动 docker
systemctl start docker
# 设置为开机启动
systemctl enable docker

# 配置阿里云镜像服务加速
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://82m9ar63.mirror.aliyuncs.com"]
}
EOF

# 重启 docker
sudo systemctl daemon-reload
sudo systemctl restart docker
```

## 五、安装 Kubernetes

### 5.1 安装K8S核心

> ❗️该步骤所有的机器节点都需要执行。
>
> 此处`k8s`版本选型建议是`1.23.x`及以下的，因为在`1.24.x`之后，`k8s`移除了`dockershim`，也不是说`docker`就不能用了，但是需要根据官方的方案进行配置，过程繁琐，这次集群搭建选用版本`1.23.9`，是`1.23.x`的最后一个版本，相对稳定一些。
>
> 本次在本地环境模拟集群，通过`kubeadm`方式引导式安装，所以希望的是快速简单的构建出集群。

```sh
# 配置K8S的yum源
cat <<EOF > /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
# 该url是intel版本的，arm架构需替换 baseurl=http://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64
baseurl=http://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-aarch64
enabled=1
gpgcheck=0
repo_gpgcheck=0
gpgkey=http://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg
       http://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg
EOF

# 检查文件是否写入
cat /etc/yum.repos.d/kubernetes.repo
```

```sh
# 卸载旧版本
yum remove -y kubelet kubeadm kubectl

# 查看可以安装的版本
yum list kubelet --showduplicates | sort -r

# 安装指定版本的 k8s 核心组件：kubelet kubeadm kubectl
yum install -y kubelet-1.23.9 kubeadm-1.23.9 kubectl-1.23.9

# 开机启动 kubelet
systemctl enable kubelet && systemctl start kubelet
```

### 5.2 初始化 Master 节点

> 以下操作在`k8s-master`节点执行

```sh
# 查看当前 kubeadm 需求的核心组件版本，接下来是安装他们
kubeadm config images list
```

```sh
# 如果能访问外网的话，则用 google 官方的镜像，否则只能去 dockerhub 搜集，或者去阿里云镜像仓库里面找找
k8s.gcr.io/kube-apiserver:v1.23.9
k8s.gcr.io/kube-controller-manager:v1.23.9
k8s.gcr.io/kube-scheduler:v1.23.9
k8s.gcr.io/kube-proxy:v1.23.9
k8s.gcr.io/pause:3.6
k8s.gcr.io/etcd:3.5.1-0
k8s.gcr.io/coredns/coredns:v1.8.6
```

```sh
# 本人之前下载了 google 的官方镜像，已经 push 到阿里云了，可以按如下步骤使用我上传阿里云的镜像

# 封装成images.sh文件
#!/bin/bash
images=(
  kube-apiserver:v1.23.9
  kube-proxy:v1.23.9
  kube-controller-manager:v1.23.9
  kube-scheduler:v1.23.9
  coredns:v1.8.6
  etcd:3.5.1-0
  pause:3.6
)
for imageName in ${images[@]} ; do
    docker pull registry.cn-hangzhou.aliyuncs.com/yiwenup/$imageName
done
# 封装结束

# 执行 images.sh
chmod +x images.sh && ./images.sh

# 需要注意的是 coredns 镜像，通过阿里云方式的情况下，下载到本地之后需要重新打 tag
docker tag registry.cn-hangzhou.aliyuncs.com/yiwenup/coredns:v1.8.6 registry.cn-hangzhou.aliyuncs.com/yiwenup/coredns/coredns:v1.8.6
```

```sh
# init 一个 master 节点
kubeadm init \
--kubernetes-version v1.23.9 \
--apiserver-advertise-address=10.211.55.8 \
--service-cidr=11.11.0.0/16 \
--pod-network-cidr=192.168.0.0/16 \
--image-repository registry.cn-hangzhou.aliyuncs.com/yiwenup \
--ignore-preflight-errors=all

# kubernetes-version：写 k8s 安装的版本
# apiserver-advertise-address：写 master 节点的 ip
# service-cidr：划定 service 负载均衡网络的子网范围，注意：不能和 apiserver-advertise-address\pod-network-cidr 重合
# pod-network-cidr：划定 pod 集群内子网范围，注意：不能和 apiserver-advertise-address\service-cidr 重合
# image-repository：如果使用阿里云方式，则需要制定一下镜像仓库
```

```sh
# 注意，在 kubeadm 引导安装下，我们需要关注初始化成功之后打印的信息，根据提示完成接下来的步骤

# 复制相关文件夹
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

# 导出环境变量
export KUBECONFIG=/etc/kubernetes/admin.conf

# 部署一个网络组件：这里选用 calico
kubectl apply -f https://docs.projectcalico.org/manifests/calico.yaml

# 检查一下集群中的节点，目前只初始化了 master，所以应该只有 master 一个节点
kubectl get nodes
```

### 5.3 加入 Worker 节点

```sh
# 同样使用 master 节点初始化成功之后打印的内容，其中有 worker 节点加入 master 的方式
kubeadm join 172.24.80.222:6443 --token nz9azl.9bl27pyr4exy2wz4 \
	--discovery-token-ca-cert-hash sha256:4bdc81a83b80f6bdd30bb56225f9013006a45ed423f131ac256ffe16bae73a20
```

```sh
# 只是 token 会过期，对于过期的情况下，我们想加入新的 worker，可以重新生成 token，尤其可以设置为不不过期
kubeadm token create --ttl 0 --print-join-command
```

### 5.4 验证集群

```sh
#获取所有节点
kubectl get nodes
```

### 5.5 设置 IPVS

```sh
kubectl edit cm kube-proxy -n kube-system
```

```yaml
# 找到 ipvs ，并设置 mode 为 ipvs
ipvs:
    excludeCIDRs: null
    minSyncPeriod: 0s
    scheduler: ""
    strictARP: false
    syncPeriod: 30s
kind: KubeProxyConfiguration
metricsBindAddress: 127.0.0.1:10249
mode: "ipvs" # 修改此处
```

```sh
# 重启 kube-proxy = 删除 kube-proxy 的 pod + k8s 自愈能力
kubectl get pod -A|grep kube-proxy
kubectl delete pod <pod 名称> -n kube-system
```

## 六、安装 Dashboard

> 安装参考地址：https://github.com/kubernetes/dashboard
>
> 版本选型参考地址：https://github.com/kubernetes/dashboard/releases

由于我们的`k8s`版本选择的是`1.23.9`，所以根据官方指导，`dashboard`的版本选择为`v2.5.1`

下载部署的描述文件：https://raw.githubusercontent.com/kubernetes/dashboard/v2.5.1/aio/deploy/recommended.yaml

修改`Service`类目，补充`type: NodePort`

执行部署`kubectl apply -f ./recommended.yaml`

之后在访问之前，需要做好权限控制，根据官方指导：https://github.com/kubernetes/dashboard/blob/master/docs/user/access-control/README.md，创建`dashboard-admin.yaml`，补充以下内容之后，执行`kubectl apply -f dashboard-admin.yaml`，如果有报错的话，先执行`kubectl delete -f dashboard-admin.yaml`后再执行`kubectl apply -f dashboard-admin.yaml`。

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: kubernetes-dashboard
  namespace: kubernetes-dashboard
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
  - kind: ServiceAccount
    name: kubernetes-dashboard
    namespace: kubernetes-dashboard
```

最后在访问的时候需要输入令牌，令牌的查询使用如下命令：

```sh
kubectl -n kubernetes-dashboard describe secret $(kubectl -n kubernetes-dashboard get secret | grep admin-user | awk '{print $1}')
```

