---
title: "Freemarker自定义指令与场景"
date: 2024-02-23T16:50:22+08:00
categories: ["技巧"]
tags: ["技巧","Freemarker"]
draft: false
code:
  copy: true
toc:
  enable: true
---

## 一、Freemarker 自定义指令

> **TemplateDirectiveModel** 接口是 Freemarker 自定标签或者自定义指令的核心处理接口。通过实现该接口，用户可以自定义标签（指令）进行任意操作， 任意文本写入模板的输出。
>
> 从某种意义上，当我们实现了自己的一套指令之后，在 .ftl 文件中使用，能对该 .ftl 输出的内容进一步扩展处理。

### 1.1 使用示例 - 扩展指令

首先我们基于`TemplateDirectiveModel`接口扩展自定义指令`MyDirective`的实现逻辑，如下示例为实现文本大写转换。

```java
public class MyDirective implements TemplateDirectiveModel {

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        if (body != null) {
            // 执行自定义指令内部逻辑，输出模版填充结果
            StringWriterWriter sw = new StringWriter();
            body.render(sw);
            String content = sw.toString();

            // 处理填充结果字符串，进行大写转换
            content = content.toUpperCase();

            // 写出转换结果
            env.getOut().write(content);
        }
    }
}
```

之后注册该自定义指令`MyDirective`到`Freemarker`上下文

```java
Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);

cfg.setSharedVariable("myDirective", new MyDirective());
```

### 1.2 使用示例 - 模版运用

> 使用上只要注意，标签的名称要和注册时的一致，并且前缀标注`@`符号，如下示例

```xml
<html>
<body>
    <h1>This is a sample template.</h1>

    <@myDirective>
        <p>Welcome, ${name}!</p>
    </@myDirective>
</body>
</html>
```

## 二、使用场景实战

> 举一个比较实际的场景，进一步加深理解。
>
> 假设当前是低代码平台翻译引擎开发场景，需要根据用户可视化操作生成的`DSL`，抽取关键要素，填充`.ftl`模版文件，最终生成`.java`源码文件。

（1）在上述场景下，由于该低代码开发自由度相对较高，并不能确定用户编排的最终逻辑中，会用到哪些类型，因此导包模块需要至少枚举出一些常见类型预置，大致`.ftl`模版文件举例如下。

```java
package ${packageName};

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
${imports}

public class ${className} {
    ${fieldDeclare}
    
    ${methodDeclare}
}
```

（2）由于`freemarker`模版引擎原生能力，仅做参数填充，生成的最终产物文件可能会是下述样子。

```java
package cloud.yiwenup.sample.hystrix.entity;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;

@Getter
@Setter
@ToString
public class UserDTO implements Serializable {
    private static final long serialVersionUID = -8515724098890484422L;
    private String code;
    private String info;
    private String name;
    private Integer age;
    private String address;
}
```

（3）如果公司内部质量卡点足够严格，甚至会运用在低代码应用上，那多半会出现`unused imported`类似的卡点，**此时需要实现一种效果：根据动态生成的`.java`源文件，动态移除未使用的包。这里就可以用到自定义指令进行后置处理，实现这种效果。**

```java
public class MyDirective implements TemplateDirectiveModel {

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        if (body != null) {
            // 执行自定义指令内部逻辑，输出模版填充结果
            StringWriterWriter sw = new StringWriter();
            body.render(sw);
            String content = sw.toString();

            // TODO 这里可以借助一些现成的类库，格式化源文件，包括移除未被引用的字符串。比如：google-java-format

            // 写出转换结果
            env.getOut().write(content);
        }
    }
}
```

```java
Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);

cfg.setSharedVariable("myDirective", new MyDirective());
```

```java
package ${packageName};
<@myDirective>
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
${imports}

public class ${className} {
    ${fieldDeclare}
    
    ${methodDeclare}
}
</@myDirective>
```

