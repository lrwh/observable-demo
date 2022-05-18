# 目录结构说明
```shell
.
├── automatic-instrumentation ------------------- [自动仪表](https://opentelemetry.io/docs/instrumentation/python/automatic/)，使用:jaeger thrift http
│   ├── client.py ------------------------- 客户端，执行后会调用服务端，端口：8082
│   ├── client.sh ------------------------- 客户端，执行脚本
│   ├── server.py ------------------------- 服务端，为客户端提供服务，模拟常规接口调用
│   └── server.sh ------------------------- 服务端，执行脚本
├── bootstrap.sh -------------------------------- 启动脚本，分别调用自动仪表、手动仪表相关的执行 shell 脚本
├── datasource.yaml ----------------------------- grafana 源数据源配置文件
├── docker-compose.yaml ------------------------- 整个模拟环境的 docker 编排文件
├── Dockerfile ---------------------------------- python 运行环境容器，定制r
├── manual-instrumentation
│   ├── jaeger_exporter_grpc.py
│   ├── jaeger_exporter_grpc.sh
│   ├── jaeger_exporter_thrift.py
│   └── jaeger_exporter_thrift.sh
├── README.md
└── tempo.yaml
```

# 镜像说明
```shell
docker pull tonycody/python3-otel:1.0
```
#该镜像已经安装了以下包信息：
```shell
pip install opentelemetry-sdk && \
pip install opentelemetry-api && \
pip install opentelemetry-distro && \
pip install opentelemetry-instrumentation-flask && \
pip install flask && \
pip install requests && \
pip install opentelemetry-exporter-jaeger-proto-grpc && \
pip install opentelemetry-exporter-jaeger-thrift
```
# 使用方式
```shell
docker-compose up | grep python3-otel-example
```
## 执行结果说明
```shell
python3-otel-example_1  |  Starting Client... 
python3-otel-example_1  | {
python3-otel-example_1  |     "name": "client-server",
python3-otel-example_1  |     "context": {
python3-otel-example_1  |         "trace_id": "0xe8d7f6bc7f85d5068c9d60e5c3f5a07a",
python3-otel-example_1  |         "span_id": "0x70da62b26ebdfa3c",
python3-otel-example_1  |         "trace_state": "[]"
python3-otel-example_1  |     },
python3-otel-example_1  |     "kind": "SpanKind.INTERNAL",
python3-otel-example_1  |     "parent_id": "0x5ca981a4d24c4d18",
python3-otel-example_1  |     "start_time": "2022-05-18T10:08:57.349127Z",
python3-otel-example_1  |     "end_time": "2022-05-18T10:08:57.396925Z",
python3-otel-example_1  |     "status": {
python3-otel-example_1  |         "status_code": "UNSET"
python3-otel-example_1  |     },
python3-otel-example_1  |     "attributes": {},
python3-otel-example_1  |     "events": [],
python3-otel-example_1  |     "links": [],
python3-otel-example_1  |     "resource": {
python3-otel-example_1  |         "telemetry.sdk.language": "python",
python3-otel-example_1  |         "telemetry.sdk.name": "opentelemetry",
python3-otel-example_1  |         "telemetry.sdk.version": "1.12.0rc1",
python3-otel-example_1  |         "service.name": "python-thrift-client-automatic",
python3-otel-example_1  |         "telemetry.auto.version": "0.31b0"
python3-otel-example_1  |     }
python3-otel-example_1  | }
```
其中的`trace_id`为链路 id，如上：`0xe8d7f6bc7f85d5068c9d60e5c3f5a07a`

## 查询链路
```shell
# 打开 grafana
http://localhost:3000/
```
> 查询时需要去掉`trace_id`前面的`0x`
![](http://cdn.0512.host/images/20220518181013.png)