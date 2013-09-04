Nutz 的Java代码结合部
=======

因为 Nutz 有很多子项目，比如

* nutz-web
* nutz-quartz
* nutz-qrcode
* nutz-video
* nutzmongo
* nutzdoc
* nutzdiff
* nutzcp
* ..

这些项目我们会做到除了 nutz.jar 不互相依赖，那么在一个应用中必然会有一些代码是要结合几个项目一起调用。
这里我们提供了一些帮助代码，可以让你更轻松的在你自己的应用中组合这些子项目

