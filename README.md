# 注意! 注意!

该项目已经被拆分并迁移到 [xwood项目](<https://github.com/xwoods/>) 中, 这里已停止更新.

# what is woods ?

"森林"中有什么,这里就有什么.....额,有点扯了....

好吧,说正经的

在编程过程中,我们总是发现一些可以复用的代码,然后就会把它提取出来

封装成函数或者封装成一个类呀对象呀等等,方便在其他方调用

我们把功能上复用的叫做 *"utility code"*

在多个项目之间或大型项目内部,我们发现了一些业务逻辑代码也可以复用

我们把业务上复用的叫做 *"common code"*

比如这里提供的:

1. 查询字符串解析
2. 常用的Util方法
3. …….
4. 等等


每个功能我们将提供一份api与各个语言的实现

目前准备实现的语言:

* java
* go
* js


大概的目录结构如下:

	[woods]
		[doc]
			[md]
				query.md
			[zdoc]
				query.zdoc
		[java]
			[query]
			......
		[golang]
			[query]
			......
		[js]
			[query]
			......
			

最后说一句, 我们会保证它就像[nutz](http://nutzam.com/)一样好用^_^






