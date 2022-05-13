
### 两种启动方式：

#### 方式一

配置环境变量

```shell
OTEL_TRACES_EXPORTER=otlp \
OTEL_EXPORTER_OTLP_ENDPOINT=http://192.168.91.11:4317 \
OTEL_EXPORTER_OTLP_TIMEOUT=10000 \
java -javaagent:opentelemetry-javaagent.jar \
-Dotel.resource.attributes=service.name=server \
-Dotel.javaagent.debug=false \
-Dotel.metrics.exporter=none \
-Dotel.propagators=b3 \
-jar springboot-server.jar --client=false
```

#### 方式二：

```shell
java -javaagent:../opentelemetry-javaagent/opentelemetry-javaagent.jar \
-Dotel.traces.exporter=otlp \
-Dotel.exporter.otlp.endpoint=http://192.168.91.11:4317 \
-Dotel.resource.attributes=service.name=server \
-Dotel.metrics.exporter=none \
-Dotel.propagators=b3 \
-jar springboot-server.jar --client=false
```

#### 配置传播器

如果需要与前端链路打通，则需要保证前后端链路采用的是同一种类型的传播器

```java
-Dotel.propagators=b3
```

#### 访问client
开启client
```
curl http://localhost:8080/setClient?c=true
```
关闭client
```
curl http://localhost:8080/setClient?c=false
```
查看当前client状态
``` shell script
curl http://localhost:8080/getClient
```

#### server-client

https://github.com/lrwh/observable-demo/springboot-client/README.MD

#### 前端链路

https://github.com/lrwh/observable-demo/opentelemetry/README.md

效果图：

![](../images/2022-03-11-11-28-16-image.png)


