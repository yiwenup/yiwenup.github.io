---
title: "01_安装 Docker 环境"
date: 2021-11-10T19:12:32+08:00
categories: ["Docker"]
tags: ["环境搭建","容器化"]
draft: false
code:
  copy: true
toc:
  enable: true
---

## 一、环境预准备

1. 移除之前安装的 Docker

   ```sh
   yum remove docker \
   docker-client \
   docker-client-lastest \
   docker-common \
   docker-lastest \
   docker-latest-logrotate \
   docker-logrotate \
   docker-engine
   ```

   或者

   ```sh
   yum remove docker*
   ```

2. 安装虚拟环境

   ```sh
   yum install -y yum-utils \
   device-mapper-persistent-data \
   lvm2
   ```

3. 配置镜像源

   ```sh
   yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
   ```

## 二、安装 Docker 环境

1. 下载并安装**最新版** Docker

   ```sh
   yum -y install docker-ce \
   docker-ce-cli \
   containerd.io
   ```

   如需安装指定版本，可以`yum list docker-ce --showduplicates | sort -r`查找可用的 Docker 版本，之后替换版本号安装`sudo yum install docker-ce-<VERSION_STRING>.x86_64 docker-ce-cli- <VERSION_STRING>.x86_64 containerd.io`

2. 启动 Docker

   ```sh
   systemctl start docker
   ```

3. 检查是否启动成功

   ```sh
   docker -v
   ```

4. 设置开机启动

   ```sh
   systemctl enable docker.service
   ```

## 三、配置镜像

> docker 默认是会去 dockhub 上拉取我们所需要的 image，但是有时候会限于网络问题，导致 image 获取过慢甚至直接访问超时，所以可以配置一个镜像来提升访问效率，此处使用的是阿里云提供的容器镜像服务

1. 配置镜像

   ```sh
   mkdir -p /etc/docker
   ```

   ```sh
   tee /etc/docker/daemon.json <<-'EOF'
   {
     "registry-mirrors": ["https://uoyq8dxj.mirror.aliyuncs.com"]
   }
   EOF
   ```
   
   ```sh
   systemctl daemon-reload
   ```
   
   ```sh
   systemctl restart docker
   ```
   
2. 测试配置是否生效

   ```sh
   docker info
   ```


## 四、安装可视化界面 Portainer

```sh
docker run -d -p 8000:8000 -p 9000:9000 --name=portainer --restart=always -v /var/run/docker.sock:/var/run/docker.sock -v portainer_data:/data portainer/portainer-ce
```

完成后直接访问 9000 端口即可

