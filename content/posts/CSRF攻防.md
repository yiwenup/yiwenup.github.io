---
title: "09 CSRF攻防"
date: 2024-01-02T10:37:34+08:00
categories: ["Web安全和加速"]
tags: ["网络安全","技巧"]
draft: false
code:
  copy: true
toc:
  enable: true
---

> 漏洞扫描可以借助工具：CSRFTester 等提前自测。

> `CSRF（Cross Site Request Forgery）`为跨站请求伪造攻击。**攻击者利用正常登录用户在浏览器上留的`cookie`认证信息，伪造正常用户的请求发送给服务端，造成攻击。**
>
> `CSRF`与`XSS`在名称上有些相似，但是本质是不一样的。
>
> - `CSRF`不要求攻击者是网站系统的正常用户，而`XSS`要求攻击者必须注册为当前网站的用户；
> - `CSRF`带有欺骗意味，伪装成正常用户发起攻击，而`XSS`是一种直接攻击的形态
> - **`CSRF`需要伪装的用户保持当前系统的登录状态，即会话有效；并且攻击者要对当前系统有一定了解才行**

## 一、攻击步骤

1. 用户正常登录A系统`http://www.normal.com/shopping.html`正常操作；

2. 攻击者发送邮件给用户，或者其他方式，诱导用户点击一个链接，比如：`http://www.hacker.com/pxxn.html`；

3. 当用户点击访问这个链接的时候，其实攻击已经完成。原理就是这个链接除了在页面上正常渲染之外，还暗埋了类似以下的代码，这类代码请求A系统，做一些转账、重置用户密码等操作。

   ```html
   <!-- 发 GET 请求可以借助原生的 HTML 标签，还能绕过浏览器跨域拦截 -->
   <img src="http://www.normal.com/resetPwd?uid=xxxxx" style='display: none;'/>
   <iframe src="http://www.normal.com/resetPwd?uid=xxxxx" style='display: none;'/>
   ```

   ```html
   <!-- 发 POST 请求，可以关联一个表单提交 -->
   <iframe src="form.html" style='display:none'></iframe>
   
   <form method='post' action='http://www.normal.com/resetPwd'>
       <input type='hidden' value='xxxxx' />
   </form>
   ```

## 二、防护策略

1. 技术上`Referer`校验

   浏览器在给服务端发送请求的时候，实际上会在请求头带着`Referer`字段，该字段标识当前请求是从`referer`地址发出的。可以在网关、过滤器之类的地方，新增校验逻辑，可以参考如下：

   ```java
   // 对于登陆之类的接口，做白名单放行
   String referer = request.getHeader("referer");
   StringBuilder sb = new StringBuilder();
   sb.append(request.getScheme()).append("://").append(request.getServerName());
   if (referer == null || referer == "" || !referer.startsWith(sb.toString())) {
   	response.setContentType("text/plain; charset=utf-8");
   	response.getWriter().write("非法访问，请通过页面正常访问！");
   	return false;
   }
   return true;
   ```

2. 业务上二次校验

   - 对于修改密码或者重置密码的业务，需要输入原密码并校验正确性
   - 对于转账支付类业务，需要输入短信验证码
