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


## 入侵式埋点

与前面件Tag有差异，前面是通过 javaagent方式配置埋点，相对来说方便管理和动态埋点，不会因为想要对特殊的请求做一些埋点而调整代码。但同时配置会比较繁琐，入侵式埋点能够很好的跟业务结合，对业务进行埋点，则需要用 dd-trace-api的方式。

以java为例
﻿
``` 
        <dependency>
﻿
            <groupId>com.datadoghq</groupId>
﻿
            <artifactId>dd-trace-api</artifactId>
﻿
            <version>0.102.0</version>
﻿
        </dependency>
```

在对应需要埋点的方法添加注解  @Trace
﻿
```java
    @Trace
    public String apiTrace(){
        return "apiTrace";
    }
```

然后在 gateway方法调用这个
﻿
```java
testService.apiTrace();
```

注意：入侵式埋点不代表应用启动的时候不需要 agent ，如果没有agent， `@Trace` 也将失效。

`@Trace` 注释具有默认操作名称 `trace.annotation`，而跟踪的方法默认具有资源。

可以修改对应的名称
```java
    @Trace(resourceName = "apiTrace",operationName = "apiTrace")
    public String apiTrace(){
        return "apiTrace";
    }
```
修改后，效果如下：
![](../images/ddtrace-001.png)