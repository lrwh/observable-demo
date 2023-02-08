maven pom.xml

```java
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-bom</artifactId>
            <version>1.9.1</version>
<!--            <version>0.17.1</version>-->
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

```java
 <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-api</artifactId>
        </dependency>
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-sdk</artifactId>
        </dependency>
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-exporter-otlp</artifactId>
        </dependency>
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-extension-annotations</artifactId>
        </dependency>
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-semconv</artifactId>
            <version>1.9.0-alpha</version>
        </dependency>
```

### 两种启动方式：

#### 方式一

配置环境变量

```shell
OTEL_TRACES_EXPORTER=otlp \
OTEL_EXPORTER_OTLP_ENDPOINT=http://192.168.91.11:4317 \
OTEL_EXPORTER_OTLP_TIMEOUT=10000 \
java -javaagent:opentelemetry-javaagent.jar \
-Dotel.resource.attributes=service.name=demo \
-Dotel.javaagent.debug=false \
-Dotel.metrics.exporter=none \
-jar springboot-opentelemetry-otlp-server.jar --trace.exporter.host=192.168.91.11 --trace.exporter.port=4317 --client=true
```

#### 方式二：

```shell
java -javaagent:../opentelemetry-javaagent/opentelemetry-javaagent.jar \
-Dotel.traces.exporter=otlp \
-Dotel.exporter.otlp.endpoint=http://192.168.91.11:4317 \
-Dotel.resource.attributes=service.name=demo \
-Dotel.metrics.exporter=none \
-jar springboot-opentelemetry-otlp-server.jar --trace.exporter.host=192.168.91.11 --trace.exporter.port=4317 --client=true
```

trace.exporter.host: exporter数据接收主机，如果是本机，就用localhost
trace.exporter.port: exporter数据接收端口

效果图：

![](../images/2022-03-11-11-28-16-image.png)

观测云效果

![](../images/2022-03-11-14-13-51-image.png)

### Baggage 用法

Baggage 可以在整个链路传播，适用于全局埋点，比如用户id埋点、用户名埋点，方便我们对某一个用户追踪。

gateway方法 set Baggage

```java
// Baggage 用法,此处set
        Baggage.current().toBuilder().put("app.username", "gateway").build().makeCurrent();
        logger.info("gateway set baggage[app.username] value: gateway");
```

resource 方法 get Baggage

```java
 // Baggage 用法,此get
        String baggage = Baggage.current().getEntryValue("app.username");
        logger.info("resource get baggage[app.username] value: {}", baggage);
```

### 通过已知的traceId和spanId,来构造一个新span。

tracer 构造 span 提供了`setParent(context)`方法，便于为自定义的 span 构造一个父 span。

```java 
tracer.spanBuilder(spanName).setParent(context)
```
其中 `setParent` 需要传入 `Context` 参数，所以需要构造一个上下文。

而 otel sdk 只提供了一个 create 方法用于创建 `SpanContext`，其中可以自定义 traceId 和 spanId。

```java
    SpanContext create(String traceIdHex, String spanIdHex, TraceFlags traceFlags, TraceState traceState)
```
但 SpanContext 并不是 Context，因而还需要做一层转换。

```java
    private Context withSpanContext(SpanContext spanContext, Context context) {
        return context.with(Span.wrap(spanContext));
    }
```
完整代码如下：

```java
    /***
     * @Description 通过已知的traceId和spanId,来构造一个新span。
     * @Param [spanName, traceId, spanId]
     * @return java.lang.String
     **/
    @GetMapping("/customSpanByTraceIdAndSpanId")
    @ResponseBody
    public String customSpanByTraceIdAndSpanId(String spanName,String traceId,String spanId){
        assert StringUtils.isEmpty(spanName):"spanName 不能为空";
        assert StringUtils.isEmpty(traceId):"traceId 不能为空";
        assert StringUtils.isEmpty(spanId):"spanId 不能为空";
        Context context =
                withSpanContext(
                        SpanContext.create(
                                traceId, spanId, TraceFlags.getSampled(), TraceState.getDefault()),
                        Context.current());
        Span span = tracer.spanBuilder(spanName)
                .setParent(context)
                .startSpan();
        span.setAttribute("attribute.a2", "some value");
        span.setAttribute("func","attr");
        span.setAttribute("app","otel3");
        span.end();
        return buildTraceUrl(span.getSpanContext().getTraceId());
    }

    private Context withSpanContext(SpanContext spanContext, Context context) {
        return context.with(Span.wrap(spanContext));
    }
```

**需要特别注意，依据当前测试写法的请求自身会产生一个新的trace信息。新构造的span是依据传入的参数进行构造。**

我们可以通过访问链接来观察结果

http://localhost:8080/customSpanByTraceIdAndSpanId?spanName=tSpan&traceId=24baeeddfbb35fceaf4c18e7cae58fe1&spanId=ff1955b4f0eacc4f

![image](../images/otel-customSpan.png)


## metric

### 使用 otel sdk 构造 metric 信息

```java
    @Bean
    public void customMetrics() {
        meter().gaugeBuilder("connections")
                .setDescription("当前Socket.io连接数")
                .setUnit("1")
                .buildWithCallback(
                        result -> {
                            System.out.println("metrics");
                            for (int i = 1; i < 4; i++) {
                                result.record(
                                        i,
                                        Attributes.of(
                                                AttributeKey.stringKey("id"),
                                                "a" + i));
                            }
                        });
    }
```

`buildWithCallback` 是一个回调方法，会通过定时的方式进行上报，默认1min一次。

效果

![otel-prom-metric](../images/otel-prom-metric.png)
