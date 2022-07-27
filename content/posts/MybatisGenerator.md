---
title: "MybatisGenerator"
date: 2022-07-26T19:48:16+08:00
categories: ["技巧"]
tags: ["Mybatis","工具","技巧"]
draft: false
code:
  copy: true
toc:
  enable: true
---

## 一、MybatisGenerator 介绍

> 官网地址：https://mybatis.org/generator/

`Mybatis`是一套半自动化的`ORM`框架，所谓半自动化，其实就是相对其他的同类别数据层框架（比如`Hibernate`）而言，他没有完全屏蔽底层`SQL`，而是预留了用户可定制的入口，可以是注解形式也可以是配置文件形式。

那么通常情况下，我们作为开发者，`CRUD`其实差别不大，基础的`DAO`层代码应该是一致，如果每次新建一张表，都要去对应建立一个实体类，去写一个接口，可能还要写`XML`文件，在表字段多的情况下，过于复杂，因此`Mybatis`官方推荐了一套根据表结构逆向生成代码的工具`MybatisGenerator`。

## 二、MybatisGenerator 使用

> 本示例基于`Maven`工程，使用`MybatisGenerator`提供的`Maven`插件`mybatis-generator-maven-plugin`

### 2.1 引入插件

```xml
<plugins>
    <plugin>
        <groupId>org.mybatis.generator</groupId>
        <artifactId>mybatis-generator-maven-plugin</artifactId>
        <version>1.4.1</version>
        <executions>
            <execution>
                <id>Generate MyBatis Artifacts</id>
                <goals>
                    <goal>generate</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <overwrite>true</overwrite>
            <verbose>true</verbose>
            <configurationFile>${basedir}/src/main/resources/mbg/generator-config.xml</configurationFile>
            <outputDirectory>${basedir}/src/main/java</outputDirectory>
        </configuration>
        <dependencies>
            <dependency>
                <groupId>mysql</groupId>
                <artifactId>mysql-connector-java</artifactId>
                <version>8.0.29</version>
                <scope>runtime</scope>
            </dependency>
        </dependencies>
    </plugin>
</plugins>
```

插件引入需要注意的一些问题：

- configurationFile：这里填写的是配置文件的路径，该配置文件中描述了`mybatis generator`在生成代码的时候所需的信息
- dependency：此处需要依赖数据库驱动，并且，如果后续使用一些基于`mbg`扩展的插件，也需要在此处做依赖引入

### 2.2 配置文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
        "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">

<generatorConfiguration>

    <context id="MySQLTables" targetRuntime="Mybatis3" defaultModelType="flat">

        <!-- 设置字符集 -->
        <property name="javaFileEncoding" value="UTF-8"/>
        <!-- 文件覆盖，不合并 -->
        <property name="isMergeable" value="false" />

        <!-- 注释 -->
        <commentGenerator>
            <property name="suppressAllComments" value="false" />
            <property name="suppressDate" value="false" />
            <property name="dateFormat" value="yyyy-MM-dd HH:mm:ss"/>
            <property name="addRemarkComments" value="true"/>
            <property name="author" value="yiwenup" />
        </commentGenerator>

        <!-- 数据库链接 -->
        <jdbcConnection driverClass="com.mysql.cj.jdbc.Driver"
                        connectionURL="url"
                        userId="user"
                        password="pwd">
            <property name="remarksReporting" value="true"/>
        </jdbcConnection>

        <javaTypeResolver>
            <property name="forceBigDecimals" value="true" />
        </javaTypeResolver>

        <!-- 实体类 -->
        <javaModelGenerator targetPackage="cloud.yiwenup.sample.mybatis.ext.entity" targetProject="./src/main/java">
            <property name="enableSubPackages" value="true" />
            <property name="trimStrings" value="true" />
        </javaModelGenerator>

        <!-- XML -->
        <sqlMapGenerator targetPackage="cloud.yiwenup.sample.mybatis.ext.inter"  targetProject="./src/main/resources">
            <property name="enableSubPackages" value="true" />
        </sqlMapGenerator>

        <!-- 接口 -->
        <javaClientGenerator type="XMLMAPPER" targetPackage="cloud.yiwenup.sample.mybatis.ext.inter"  targetProject="./src/main/java">
            <property name="enableSubPackages" value="true" />
        </javaClientGenerator>

        <!-- 表 -->
        <table tableName="activity" domainObjectName="Activity" />
    </context>
</generatorConfiguration>
```

配置文件需要注意的问题：

- 每个标签都是按顺序编排的，不能随意改动标签的位置
- `javaClientGenerator`的`type`属性，推荐用`XMLMAPPER`，因为这种风格能将`SQL`脚本与`Java`代码解耦，`SQL`写在`XML`文件中
- `context`的`targetRuntime`需要选择`Mybatis3`



