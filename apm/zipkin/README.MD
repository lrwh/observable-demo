

## Docker 安装 zipkin

```shell
docker run -d --name zipkin -p 9411:9411 openzipkin/zipkin:latest
```



## Zipkin-UI

通过 http://zipkin-server:9411 访问 zipkin-ui



### 列表

![zipkin-index](../../images/zipkin-index.png)

### 链路火焰图

![zipkin-ui](../../images/zipkin-ui.png)


