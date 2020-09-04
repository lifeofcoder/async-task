# 基于Disruptor的本地异步任务执行框架
取代本地通过普通BlockingQueue的方式实现生产者消费者等异步场景。

## 使用方法
请参考AsyncTaskEngineTest.java类的测试用例。

## 基本原理
通过封装Disruptor让本地异步任务开发变得更加简单快捷，且任务执行更加高效（基于Disruptor的性能）。
通过异常重试，异常存储，异常告警等机制确保任务不丢失（100%执行成功），
详细原理介绍请访问[基于Disruptor的本地异步任务执行框架原理与基本使用](https://blog.csdn.net/hilaryfrank/article/details/108052182 '原理与使用')。 

## License
[Apache License 2.0](https://github.com/lifeofcoder/dynamic-executor/blob/master/LICENSE)
禁止商用，个人引用请标明出处。

## 联系我
如果有任何疑问或者高见，欢迎添加微信公众号"Life of Coder"共同交流探讨。
<p align="center"><img width="40%" src="https://img-blog.csdnimg.cn/20191128202145538.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2hpbGFyeWZyYW5r,size_16,color_FFFFFF,t_70" /></p>
