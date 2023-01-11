## springboot-ddtrace-server 

主要演示ddtrace 的一些高级用法

当前案例使用 ddtrace 版本 0.114.0（最新版本）进行测试

博客文档：https://docs.guance.com/best-practices/monitoring/ddtrace-skill/

启动

``` shell script
java -javaagent:D:/ddtrace/dd-java-agent-0.114.0.jar \
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
        <groupId>com.datadoghq</groupId>
        <artifactId>dd-trace-api</artifactId>
        <version>0.114.0</version>
    </dependency>
    <dependency>
        <groupId>io.opentracing</groupId>
        <artifactId>opentracing-api</artifactId>
        <version>0.33.0</version>
    </dependency>
    <dependency>
        <groupId>io.opentracing</groupId>
        <artifactId>opentracing-util</artifactId>
        <version>0.33.0</version>
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

## 让业务关键tag在后端链路进行传递

ddtrace 提供了 Baggage 方式，让指定的tag在链路上进行传递。比如用户名、岗位等信息，方便分析用户行为。

> span.setBaggageItem("username","liurui");

接口请求，携带两个header：dd-username、dd-job

![](../images/ddtrace-header-tag.png)

### datakit 配置  
这里需要配合 datakit 的ddtrace采集器配置一起使用  
```toml
customer_tags = ["username", "job"]
```

在观测云上的效果

![](../images/ddtrace-baggage-tag.gif)


## 重写 span

通常，应用会对业务逻辑进行异常处理，相关的链路可能因为无法标记为 error 从而导致无法正常统计到应用的 error trace，一般为 `try-catch` 或者全局异常捕获。这时候就需要对链路进行标记处理，可以通过自定义 span 的方式来标记这些 span 为 error span，只需要在 catch 处进行标记即可.

通过以下方式获取当前 span 信息

> final Span span = GlobalTracer.get().activeSpan();

标记 span 为 error

> span.setTag(Tags.ERROR, true);

如果当前方法是放在catch里面，则可以将 track 信息也可以输出到 span 里面。

可以将 error span 处理逻辑作为一个公共的函数共全局使用，代码如下所示：

``` java hl_lines="4 6 7 11"
    private void buildErrorTrace(Exception ex) {
        final Span span = GlobalTracer.get().activeSpan();
        if (span != null) {
            span.setTag(Tags.ERROR, true);
            span.log(Collections.singletonMap(Fields.ERROR_OBJECT, ex));
            span.setTag(DDTags.ERROR_MSG, ex.getMessage());
            span.setTag(DDTags.ERROR_TYPE, ex.getClass().getName());

            final StringWriter errorString = new StringWriter();
            ex.printStackTrace(new PrintWriter(errorString));
            span.setTag(DDTags.ERROR_STACK, errorString.toString());
        }

    }
```

调用方代码

``` java hl_lines="10"
@GetMapping("/gateway")
    @ResponseBody
    public String gateway(String tag) {
        ......
        try {
            if (client) {
                httpTemplate.getForEntity("http://" + extraHost + ":8081/client", String.class).getBody();
            }
        } catch (Exception e) {
            buildErrorTrace(e);
        }
        return httpTemplate.getForEntity(apiUrl + "/billing?tag=" + tag, String.class).getBody();
    }

```

## 自定义 span

通过自定义 span，能够让用户自定义业务链路数据。

```java
    // 获取一个 tracer 对象
        Tracer tracer = GlobalTracer.get();
    // 获取当前 span
    final Span span = GlobalTracer.get().activeSpan();
    // 创建三个子span
    for (int i = 0; i <3 ; i++) {
        Span serverSpan = tracer.buildSpan("spanName" + i) // 配置span的名称
        .withTag("service_name", "someService" + i) // 配置span tag
        .asChildOf(span) // 设置span的 父span
        .start(); // 开启一个span
        tracer.activateSpan(serverSpan).close();
        serverSpan.finish();
    }
```