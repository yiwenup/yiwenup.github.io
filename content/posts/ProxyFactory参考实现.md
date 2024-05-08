---
title: "02 ProxyFactory参考实现"
date: 2022-10-30T13:45:20+08:00
categories: ["JPA"]
tags: ["Spring生态","JPA","AOP"]
draft: false
code:
  copy: true
toc:
  enable: true
---

> `Spring Data JPA`支持开发者在仅定义接口的情况下，能提供出通用的方法实现，这一点在之前源码跟踪的时候也猜测到了是采用动态代理机制实现的，并且在后续也确认了是通过`ProxyFactory`实现的
>
> `ProxyFactory`是`Spring`提供的一种`AOP`的编程式实现方式

1. 创建一个顶层接口以及通用默认实现。顶层接口类似于`org.springframework.data.repository.Repository`，而默认的实现类似于`org.springframework.data.jpa.repository.support.SimpleJpaRepository`

   ```java
   public interface IService {
       String sayHello(String name);
   }
   ```

   ```java
   public class SimpleImpl implements IService {
       @Override
       public String sayHello(String name) {
           return "hi " + name;
       }
   }
   ```

2. 创建一个测试接口，仅继承`IService`，没有任何方法实现

   ```java
   public class TestService extends IService {
   }
   ```

3. 创建测试方法，通过`ProxyFactory`为`TestService`生成代理，具体方法执行交给`SimpleImpl`实现

   ```java
   public class TestMain {
       public static void main(String[] args) {
           ProxyFactory proxyFactory = new ProxyFactory();
           proxyFactory.setTarget(new SimpleImpl());
           proxyFactory.setInterfaces(TestService.class);
           TestService testService = (TestService) proxyFactory.getProxy();
           System.out.printlin(testService.sayHello("zhangsan")); // hi zhangsan
       }
   }
   ```

   
