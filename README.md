#  JSF框架的简单Demo

项目描述：JSF框架的简单Demo，用于学习和研究JSF的反序列化漏洞。

JavaServer Faces (JSF) 是一种用于构建 Web 应用程序的标准。JSF标准有俩个常见的实现框架：

- Mojarra(com.sun.faces)实现了JSF的框架。在其2.1.29-08、2.0.11-04版本之前，没有对JSF中ViewState进行加密，导致攻击者可以构造恶意的序列化ViewState对象对服务器进行攻击。JSF2.2之前的规范要求实现加密机制，但是并不要求使用加密机制。在JSF2.2之后要求使用加密机制，但是如果能获取到加密密钥依然可以利用，默认情况下，Mojarra 使用AES加密算法HMAC-SHA256验证ViewState。其实可以类比为Shiro550硬编码问题。
- MyFaces也是实现了JSF的框架。

JSF框架的反序列化需要开启ViewState配置：

- Mojarra的默认javax.faces.STATE_SAVING_METHOD设置是server，开发人员需要手动将其更改为client。Mojarra才能进行利用。如果将序列化的 ViewState 发送到服务器，但Mojarra使用的是server配置则ViewState 保存它，不会尝试反序列化它。
- MyFaces的默认javax.faces.STATE_SAVING_METHOD设置是server。但是MyFaces无论值是client或者是server，都可以进行反序列化。

# 0x01 寻找JSF反序列化漏洞

如何在代码审计中快速寻找系统是否有JSF反序列化漏洞：

- 在web.xml中搜索是否存在关键字javax.faces.webapp.FacesServlet，一般来说JSF的密钥Key也会配置在Web.xml文件中。

```xml
<servlet>
    <servlet-name>Faces Servlet</servlet-name>
    <servlet-class>javax.faces.webapp.FacesServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
</servlet>

<!-- Map these files with JSF -->
<servlet-mapping>
	<servlet-name>Faces Servlet</servlet-name>
	<url-pattern>/faces/*</url-pattern>
</servlet-mapping>
<servlet-mapping>
	<servlet-name>Faces Servlet</servlet-name>
	<url-pattern>*.jsf</url-pattern>
</servlet-mapping>
<servlet-mapping>
	<servlet-name>Faces Servlet</servlet-name>
	<url-pattern>*.faces</url-pattern>
</servlet-mapping>
<servlet-mapping>
	<servlet-name>Faces Servlet</servlet-name>
	<url-pattern>*.xhtml</url-pattern>
</servlet-mapping>
```

找到配置后还需要确认开启viewSate配置才能进行反序列化利用

```xml
  <context-param>
    <param-name>javax.faces.STATE_SAVING_METHOD</param-name>
    <param-value>client</param-value>
  </context-param>
```

# 0x02 JSF反序列化利用

faces的反序列化分为有加密和无加密俩种情况，JSF处理逻辑如下：具体是否使用AES加密需要看lib版本和配置。

```java
Generate Payload: writeObject --> Gzip --> Encrpt --> Base64Encode
Recive Payload: Base64Decode --> Decrpt --> UnGzip --> readObject
```

存在加密的情况的话具体怎么配置AES的key，根据引入的组件版本不同，配置方式也是不同的，关于Mojarra框架的Key配置可以看：https://stackoverflow.com/questions/28231372/com-sun-faces-clientstatesavingpassword-recommendations-for-actual-password

```xml
  <env-entry> 
    <env-entry-name>com.sun.faces.ClientStateSavingPassword</env-entry-name> 
    <env-entry-type>java.lang.String</env-entry-type> 
    <env-entry-value>[some secret password]</env-entry-value>
  </env-entry>
```

或

```xml
<context-param>
  <param-name>com.sun.faces.ClientSideSecretKey</param-name>
  <param-value>[some secret password]</param-value>
</context-param>
```

获取加解密方式：JSF有关加解密逻辑均在com.sun.faces.renderkit.ByteArrayGuard类的encrypt和decrypt中，需要时扣代码即可。在 https://github.com/B0T1eR/ysoSimple 工具中也集成了JSF框架的加解密方式。

#  0x03 Reference

https://www.cnblogs.com/CoLo/p/16886829.html

https://www.cnblogs.com/nice0e3/p/16205220.html

https://github.com/vulhub/vulhub/blob/master/mojarra/jsf-viewstate-deserialization/README.zh-cn.md

https://stackoverflow.com/questions/28231372/com-sun-faces-clientstatesavingpassword-recommendations-for-actual-password