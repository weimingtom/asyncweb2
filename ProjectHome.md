<p />

&lt;B&gt;

历史：

&lt;/B&gt;

<br />
1，最早 AsyncWeb 在07年中开发到0.9版本后，就停滞了。<br />
2，之后并入 apache/mina 成为 sandobx 项目，但很难找到。<br />
3，之后迁入 mina-filter-codec-http 但也没有正式发布版。<br />
4，从 PlayFramework 中剥离出 AsyncWeb 的某个版本。<br />

<p />【虎.无名】用Java实现HTTP服务的很多，但大部分都只是简单实现，很少有高性能的实现。最后聚焦到AsyncWeb上，但是这个在07年中开发到0.9版本后，似乎已经停滞了，无法找到对应src和jar来下载，链接都是无效的。后来在一个play框架中找到了一个发布版的asyncweb-common-0.9.0-SNAPSHOT.jar，然后根据这个又找到了asyncweb-core-0.9.0-20061129.082938-1.jar和asyncweb-core-0.9.0-20070614.034125-7.jar等二进制版本，但后者更旧。继续搜索，发现是AsyncWeb加入到apache的的sandbox沙箱，然后又合并到mina-filter-codec-http中了。但该项目最近修改也是Mon Nov 19 03:41:14 2007 UTC，也仅仅支持mina1.x版本，看来只能自己动手修改来支持mian2.x了。
<br />

<p />

&lt;B&gt;

问题：

&lt;/B&gt;

<br />
0，在 Mina1 中有对HTTP协议的直接支持。<br />
0，在 Mina2 中没有对HTTP协议的直接支持。而样例中，基于StreamIoHandler 的流式实现，在并发大的时候性能不佳，而且容易受到DoS攻击。<br />
1，与 mina2 m3 之后的版本不兼容。由于 Mina-2.0.0m3 代码中类和包名字有较大改动，原有代码需要做很多修改。<br />
2，与 mina2 rc 之后的版本不兼容。由于 Mina-2.0.0rc 版后，其ProtocolCodecFilter获取解析器的策略发生变更，不再保证同一IoSession使用同一个Decoder实例，导致在一个HTTP请求分多次到达时，无法解析成功，最终导致客户端超时。<br />
3，容错能力差，对于一些不规范的http请求或响应，有内存溢出现象。例如，请求长度不对，不符合HTTP报文规范。<br />

<p />

&lt;B&gt;

目标：

&lt;/B&gt;

<br />
1，DONE 基于asyncweb-0.9.0（for mina2.0.0.m2）的基础上，提供HTTP解析器，并支持mina2.0.0.m4版本【更新20120920，支持mina2.0.4+版本】<br />
2，DONE 定义HttpCodecFactory2修复一个请求多个IP报文的场景。<br />
3，DONE 提供容错能力，处理个别非法HTTP请求或响应，主要是head和body之间无\r\n分隔符的情况。<br />
4，TODO 其他。。。<br />
5，TODO 基于 JDK7 的 AIO 技术实现一些新的 IoService 组件。<br />