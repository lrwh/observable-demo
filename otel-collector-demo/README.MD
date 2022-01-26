# 本demo为otel-collector-demo

## 服务列表

| 服务名称                                   | 端口        | 描述                       |
| -------------------------------------- | --------- | ------------------------ |
| springboot_opentelemetry_jaeger_server | 8080      | app                      |
| springboot_opentelemetry_jaeger_client | 8081      | app(可以只用server)          |
| jaeger                                 |           | 端口查看 docker-compose.yml  |
| zipkin                                 | 9411:9411 | openzipkin/zipkin:latest |
| otel-collector                         |           | 端口查看 docker-compose.yml  |
| prometheus                             | 9090:9090 | prom/prometheus:latest   |

## 架构

![](C:\Users\lenovo\AppData\Roaming\marktext\images\2022-01-11-11-34-25-image.png)

### 架构说明

1、应用server和client将数据通过otlp-exporter push 到 otel-collector

2、otel-collector做数据处理后，将数据push到jaeger和zipkin

3、同时Prometheus从otel-collector pull数据。



otel-collector 配置了三个exporter.

```yaml
exporters:
  prometheus:
    endpoint: "0.0.0.0:8889"
    const_labels:
      label1: value1
  logging:

  zipkin:
    endpoint: "http://otel_collector_zipkin:9411/api/v2/spans"
    format: proto

  jaeger:
    endpoint: otel_collector_jaeger:14250
    tls:
      insecure: true
```



## 请求server，点击‘获取资源 - 正常测试’，产生一个traceId.

![](C:\Users\lenovo\AppData\Roaming\marktext\images\2022-01-26-14-27-23-image.png)

![](C:\Users\lenovo\AppData\Roaming\marktext\images\2022-01-11-11-44-56-image.png)

## 将traceId复制到zipkin查找链路信息



![](C:\Users\lenovo\AppData\Roaming\marktext\images\2022-01-11-11-44-44-image.png)



## 将traceId复制到jaeger查找链路信息



![](C:\Users\lenovo\AppData\Roaming\marktext\images\2022-01-11-11-44-24-image.png)
