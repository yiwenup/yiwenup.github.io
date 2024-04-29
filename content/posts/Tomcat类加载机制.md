---
title: "03 Tomcat类加载机制"
date: 2022-09-20T22:22:43+08:00
categories: ["Tomcat"]
tags: ["Tomcat","应用服务器"]
draft: false
code:
  copy: true
toc:
  enable: true
---

## 一、JVM类加载回顾

> 在 JVM 原本的类加载机制中，会有 BoostrapClassLoader、ExtClassLoader、AppClassLoader/SystemClassLoader 三种类加载器，他们之间是一种树形的关系，分别对应加载各自位置的类。

![image-20240428215739785](D:\notes\yiwenup.github.io\content\images\image-20240428215739785.png)

| 类加载器                                               | 功能描述                                                     |
| ------------------------------------------------------ | ------------------------------------------------------------ |
| BootstrapClassLoader<br />（引导类加载器）             | C++编写。**加载的是Java的核心类库，比如 rt.jar 中的 java.* 包下面的类。**以及构造出ExtClassLoader和AppClassLoader/SystemClassLoader。 |
| ExtClassLoader<br />（扩展类加载器）                   | Java编写。加载扩展库 JAVA_HOME/lib/ext/ 目录下的 jar 的类，比如 classpath 中的 jre、javax.* 或 java.ext.dir 参数指定位置的类。 |
| AppClassLoader/SystemClassLoader<br />（系统类加载器） | 默认类加载器。用于加载 classpath 路径下的类。                |

## 二、双亲委派机制

> 我们可以基于 JVM 提供的三种类加载，额外再扩展自定义的类加载器，用于加载自己所期望位置的类。那么对于三种类加载器以及开发者可能会自定义的类加载器，JVM 是通过**双亲委派机制**来编排不同的类对应于不同的类加载器完成类的加载。

![image-20240428220640222](D:\notes\yiwenup.github.io\content\images\image-20240428220640222.png)

双亲委派机制的主要工作流程描述如下：

- 当 JVM 准备加载某个类的时候，首先类加载器会至少从自定义类加载器开始，将类加载请求逐层上抛。比如在有自定义类加载的场景下，会将类加载的请求向上委托给父类加载器 ApplicationClassLoader/SystemClassLoader
- 类加载请求最终会到达顶层类加载器 BootstrapClassLoader，此时 BootstrapClassLoader 会尝试加载这个类，如果当前需要加载的类并不在对应类加载器期望的加载路径中，则又会向下逐层委托加载请求
- 直到某层的类加载器能够加载当前类时，类才能被加载到 JVM 中。否则，会抛出 ClassNotFound 异常。

双亲委派机制的作用：

- 防止加载同一个类，保证数据安全。通过双亲委派，我们的类总是会被顶层类加载器优先加载，上层类加载器处理不了的才会向下委派加载；并且对于上层类加载器已加载的类，后续的类加载器则不再重复加载。
- 保证核心类不会被篡改，即使篡改了也会因为父类加载器加载过而不再重复加载。

## 三、Tomcat类加载机制

> Tomcat 在 JVM 的类加载机制上做了一些调整，**不再严格遵循 JVM 的双亲委派机制**。
>
> 原因是 Tomcat 需要实现能够在 webapps 目录下同时支持部署多个应用，不同的应用之间可能依赖了相同的 jar 包，只是版本不一样，jar 包中类文件的内容会有所差异，**需要保证各应用都能加载到对应依赖的 jar 中的类文件**。
>
> 为解决上述问题，打破双亲委派机制，**Tomcat 在 JVM 的类加载器基础之上，又新增了四种类加载器：CommonClassLoader、CatalinaClassLoader、SharedClassLoader、WebAppClassLoader**。

![image-20240429121035425](D:\notes\yiwenup.github.io\content\images\image-20240429121035425.png)

| 类加载器                         | 功能描述                                                     |
| -------------------------------- | ------------------------------------------------------------ |
| BootstrapClassLoader             | 保持在原生 JVM 中的作用不变                                  |
| ExtClassLoader                   | 保持在原生 JVM 中的作用不变                                  |
| AppClassLoader/SystemClassLoader | 默认是加载 classpath 路径下的类文件。在 Tomcat 中，通过 CATALINA_HOME/bin/catalina.sh(bat) 启动脚本指定该类加载器加载 Tomcat 的启动类，比如 boostrap.jar 中的类文件 |
| CommonClassLoader                | 用于加载 Tomcat 和 web 应用共同使用的一些类文件，位于 CATALINA_HOME/lib/ 下，比如 servlet-api.jar |
| CatalinaClassLoader              | 仅用于加载 Tomcat 服务器内部本身的类，这些类对 web 应用是不可见的，无法访问到。 |
| SharedClassLoader                | 仅用于加载 web 应用共享使用的类，这些类对于 Tomcat 服务器是不可见的，无法访问到。 |
| WebAppClassLoader                | 每个 web 应用都对应拥有唯一一个 WebAppClassLoader，用于加载 web 应用 /WEB-INF/lib 和 /WEB-INF/classes 下的类 |

在 Tomcat 8.5.x 已经不再严格遵从双亲委派机制了，主要流程如下描述：

![image-20240429121702375](D:\notes\yiwenup.github.io\content\images\image-20240429121702375.png)

1. 首先类加载请求还是保持和双亲委派机制下的一致，都是逐层向上委托类加载请求，直到 BootstrapClassLoader
2. 由 BootstrapClassLoader 优先加载指定位置的类
3. 对于 BootstrapClassLoader 没有加载到的类，优先委托给 WebAppClassLoader 加载
4. WebAppClassLoader 按顺序加载 /WEB-INF/classes/ 和 /WEB-INF/lib/ 目录下的类
5. 对于 WebAppClassLoader 没有加载到的类，则按之前双亲委派的顺序，依次再委托给 ExtClassLoader、AppClassLoader/SystemClassLoader、CommonClassLoader、SharedClassLoader
