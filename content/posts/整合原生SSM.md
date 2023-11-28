---
title: "整合原生 SSM"
date: 2022-10-20T19:12:32+08:00
categories: ["技巧"]
tags: ["Spring生态","MVC","Spring","Mybatis"]
draft: false
code:
  copy: true
toc:
  enable: true

---

## 整合原生 SSM

> `SSM`指代`SpringMVC`、`Spring`、`Mybatis`，本次尝试以原生的方式进行整合，一方面是回顾历史了解`SSM`的过往，另一方面是基于整合的过程，能对底层有一个理解。

### 一、搭建 Spring 环境

> `spring`提供了`IOC`和`AOP`的概念，这将是后续框架整合的核心，因此首先需要将`spring`的环境搭建起来，便于后续整合的过程中，产生的一些`Bean`能够托管给`spring`进行管理。

1. `spring-context`依赖引入

   ```xml
   <dependency>
       <groupId>org.springframework</groupId>
       <artifactId>spring-context</artifactId>
   </dependency>
   ```

2. `resources/app-context.xml`中配置包扫描路径

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <beans xmlns="http://www.springframework.org/schema/beans"
          xmlns:context="http://www.springframework.org/schema/context"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="
          http://www.springframework.org/schema/beans
          http://www.springframework.org/schema/beans/spring-beans.xsd
          http://www.springframework.org/schema/context
          http://www.springframework.org/schema/context/spring-context.xsd
   ">
       
       <!-- bean的包扫描路径 -->
       <context:component-scan base-package="cloud.yiwenup.sample.ssm"/>
   
   </beans>
   ```

3. 引入测试依赖`spring-test`和`junit`，建立测试`bean`

   ```xml
   <dependency>
       <groupId>junit</groupId>
       <artifactId>junit</artifactId>
       <scope>test</scope>
   </dependency>
   <dependency>
       <groupId>org.springframework</groupId>
       <artifactId>spring-test</artifactId>
       <scope>test</scope>
   </dependency>
   ```

   ```java
   package cloud.yiwenup.sample.ssm.service;
   
   public interface HelloService {
       String sayHello();
   }
   ```

   ```java
   package cloud.yiwenup.sample.ssm.service.impl;
   
   import cloud.yiwenup.sample.ssm.service.HelloService;
   
   public class HelloServiceImpl implements HelloService {
       @Override
       public String sayHello() {
           return "111";
       }
   }
   ```

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <beans xmlns="http://www.springframework.org/schema/beans"
          xmlns:context="http://www.springframework.org/schema/context"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="
          http://www.springframework.org/schema/beans
          http://www.springframework.org/schema/beans/spring-beans.xsd
          http://www.springframework.org/schema/context
          http://www.springframework.org/schema/context/spring-context.xsd
   ">
   
       <!-- 定义service层Bean -->
       <bean id="helloService" class="cloud.yiwenup.sample.ssm.service.impl.HelloServiceImpl"/>
   
       <!-- bean的包扫描路径 -->
       <context:component-scan base-package="cloud.yiwenup.sample.ssm"/>
   
   </beans>
   ```

   ```java
   @RunWith(SpringJUnit4ClassRunner.class)
   @ContextConfiguration("classpath*:app-context.xml")
   public class SSMTest {
   
       @Autowired
       private HelloService helloService;
   
       @Test
       public void testIoc() {
           System.out.println(helloService.sayHello());
       }
   }
   ```

### 二、集成 Mybatis 持久层

> `spring`整合`mybatis`可以参考`mybatis`的官方集成方案：https://mybatis.org/spring/zh/index.html

1. 引入`mybatis`核心依赖

   ```xml
   <dependency>
       <groupId>org.mybatis</groupId>
       <artifactId>mybatis</artifactId>
       <version>3.5.14</version>
   </dependency>
   ```

2. 引入`spring`对`jdbc`支持以及`mybatis-spring`整合依赖

   ```xml
   <dependency>
       <groupId>org.springframework</groupId>
       <artifactId>spring-jdbc</artifactId>
   </dependency>
   <dependency>
       <groupId>org.mybatis</groupId>
       <artifactId>mybatis-spring</artifactId>
       <version>2.1.1</version>
   </dependency>
   ```

3. 引入数据库连接池`druid`和数据库驱动（这里使用`pgsql`）

   ```xml
   <dependency>
       <groupId>org.mybatis</groupId>
       <artifactId>mybatis</artifactId>
       <version>3.5.14</version>
   </dependency>
   <dependency>
       <groupId>org.postgresql</groupId>
       <artifactId>postgresql</artifactId>
       <scope>runtime</scope>
   </dependency>
   ```

4. 配置`app-context.xml`

   ```xml
   <!-- 注册数据源 -->
   <bean id="datasource" class="com.alibaba.druid.pool.DruidDataSource">
       <property name="driverClassName" value="${datasource.driver}"/>
       <property name="url" value="${datasource.url}"/>
       <property name="username" value="${datasource.username}"/>
       <property name="password" value="${datasource.password}"/>
   </bean>
   <!-- 注册 mybatis 扩展的 FactoryBean -->
   <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
       <!-- 引用数据源 -->
       <property name="dataSource" ref="datasource"/>
       <!-- 配置 Mapper.xml 扫描路径 -->
       <property name="mapperLocations" value="classpath*:cloud/yiwenup/sample/ssm/dao/mapper/**/*Mapper.xml"/>
   </bean>
   <!-- 注册 Mapper 接口扫描策略 -->
   <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
       <!-- 配置接口包扫描路径 -->
       <property name="basePackage" value="cloud.yiwenup.sample.ssm.dao.mapper"/>
       <!-- 指定接口由上述 sqlsessionfactory 统一代理实现 -->
       <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
   </bean>
   ```

5. 测试

   ```java
   package cloud.yiwenup.sample.ssm.dao.mapper;
   
   import cloud.yiwenup.sample.ssm.dao.pojo.User;
   
   import java.util.List;
   
   public interface UserMapper {
       /**
        * 查询所有
        *
        * @return 用户列表
        */
       List<User> findAll();
   }
   ```

   ```java
   package cloud.yiwenup.sample.ssm.dao.pojo;
   
   @Data
   public class User {
       private Long id;
       private String username;
       private String password;
   }
   ```

   ```xml
   <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
           "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
   <mapper namespace="cloud.yiwenup.sample.ssm.dao.mapper.UserMapper">
       <select id="findAll" resultType="cloud.yiwenup.sample.ssm.dao.pojo.User">
           select * from tab_user;
       </select>
   </mapper>
   ```

### 三、集成 Spring MVC 表现层

1. 引入依赖

   ```xml
   <!-- 整合spring mvc -->
   <dependency>
       <groupId>org.springframework</groupId>
       <artifactId>spring-webmvc</artifactId>
   </dependency>
   <dependency>
       <groupId>javax.servlet</groupId>
       <artifactId>javax.servlet-api</artifactId>
       <scope>provided</scope>
   </dependency>
   <dependency>
       <groupId>com.fasterxml.jackson.core</groupId>
       <artifactId>jackson-databind</artifactId>
   </dependency>
   <!--...-->
   <plugin>
       <groupId>org.apache.tomcat.maven</groupId>
       <artifactId>tomcat7-maven-plugin</artifactId>
       <version>2.2</version>
       <configuration>
           <path>/</path>
           <port>8899</port>
       </configuration>
   </plugin>
   ```

2. 注册注解驱动

   ```xml
   <!-- region 集成 Spring MVC -->
   <mvc:annotation-driven/>
   <!-- endregion -->
   ```

3. 配置`web.xml`

   ```xml
   <!DOCTYPE web-app PUBLIC
           "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"
           "http://java.sun.com/dtd/web-app_2_3.dtd" >
   
   <web-app>
       <display-name>Archetype Created Web Application</display-name>
   
       <!-- 必须要，否则启动时容器会去 WEB-INF 目录找 applicationContext.xml -->
       <context-param>
           <param-name>contextConfigLocation</param-name>
           <param-value>classpath*:applicationContext*.xml</param-value>
       </context-param>
   
       <!-- 注册监听器，容器启动时 -->
       <listener>
           <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
       </listener>
   
       <!-- 注册 DispatcherServlet -->
       <servlet>
           <servlet-name>dispatcher-servlet</servlet-name>
           <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
           <init-param>
               <param-name>contextConfigLocation</param-name>
               <param-value>classpath:app-context.xml</param-value>
           </init-param>
       </servlet>
       <!-- 配置 DispatcherServlet 拦截规则 -->
       <servlet-mapping>
           <servlet-name>dispatcher-servlet</servlet-name>
           <url-pattern>/</url-pattern>
       </servlet-mapping>
   
   </web-app>
   ```

4. 测试

   ```java
   package cloud.yiwenup.sample.ssm.controller;
   
   import cloud.yiwenup.sample.ssm.dao.pojo.User;
   import cloud.yiwenup.sample.ssm.service.UserService;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.stereotype.Controller;
   import org.springframework.web.bind.annotation.GetMapping;
   import org.springframework.web.bind.annotation.RequestMapping;
   import org.springframework.web.bind.annotation.ResponseBody;
   
   import java.util.List;
   
   @Controller
   @RequestMapping("/user")
   public class UserController {
   
       @Autowired
       private UserService userService;
   
       @GetMapping(value = "/getUserList")
       @ResponseBody
       public List<User> getUserList() {
           return userService.getUserList();
       }
   }
   ```

   ```jsp
   <%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" language="java" %>
   <html>
   <body>
   <fieldset>
       <legend>测试请求</legend>
       <a href="${pageContext.request.contextPath}/user/getUserList">点击测试</a>
   </fieldset>
   </body>
   </html>
   ```

   