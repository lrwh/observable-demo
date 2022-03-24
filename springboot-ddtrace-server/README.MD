## springboot-ddtrace-server 

主要演示ddtrace 的一些高级用法

当前案例使用 ddtrace 版本0.78.3和 0.97.0（最新版本）进行测试

博客文档：https://www.yuque.com/dataflux/bp/rwsgsi

启动

``` shell script
java -javaagent:D:/ddtrace/dd-java-agent-0.97.0.jar \
-Ddd.service=ddtrace-server \
-Ddd.agent.port=9529 \
-jar springboot-ddtrace-server.jar
```
访问
http://localhost:8080

