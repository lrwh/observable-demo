## Docker 安装 ELK

采用 Docker 安装 ELK ,简单又方便，相关组件版本为`7.16.2`。

### 拉取镜像

```shell
docker pull docker.elastic.co/elasticsearch/elasticsearch:7.16.2
docker pull docker.elastic.co/logstash/logstash:7.16.2
docker pull docker.elastic.co/kibana/kibana:7.16.2
```

### 配置目录

```shell
# Linux 特有配置
sysctl -w vm.max_map_count=262144
sysctl -p
# Linux 配置结束

mkdir -p ~/elk/elasticsearch/plugins
mkdir -p ~/elk/elasticsearch/data
mkdir -p ~/elk/logstash
chmod 777 ~/elk/elasticsearch/data
```

### Logstash配置

```shell
input {
  tcp {
    mode => "server"
    host => "0.0.0.0"
    port => 4560
    codec => json_lines
  }
}
output {
  elasticsearch {
    hosts => "es:9200"
    index => "springboot-logstash-demo-%{+YYYY.MM.dd}"
  }
}
```

input 参数说明：

> tcp : 为tcp协议。
> port： tcp 端口
> codec：json行解析

### Docker-compose 配置

```shell
version: '3'
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.16.2
    container_name: elasticsearch
    volumes:
      - ~/elk/elasticsearch/plugins:/usr/share/elasticsearch/plugins #插件文件挂载
      - ~/elk/elasticsearch/data:/usr/share/elasticsearch/data #数据文件挂载
    environment:
      - "cluster.name=elasticsearch" #设置集群名称为elasticsearch
      - "discovery.type=single-node" #以单一节点模式启动
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m" #设置使用jvm内存大小
      - "ingest.geoip.downloader.enabled=false" # (Dynamic, Boolean) If true, Elasticsearch automatically downloads and manages updates for GeoIP2 databases from the ingest.geoip.downloader.endpoint. If false, Elasticsearch does not download updates and deletes all downloaded databases. Defaults to true.
    ports:
      - 9200:9200
  logstash:
    image: docker.elastic.co/logstash/logstash:7.16.2
    container_name: logstash
    volumes:
      - ~/elk/logstash/logstash.conf:/usr/share/logstash/pipeline/logstash.conf #挂载logstash的配置文件
    depends_on:
      - elasticsearch #kibana在elasticsearch启动之后再启动
    links:
      - elasticsearch:es #可以用es这个域名访问elasticsearch服务
    ports:
      - 4560:4560
  kibana:
    image: docker.elastic.co/kibana/kibana:7.16.2
    container_name: kibana
    depends_on:
      - elasticsearch #kibana在elasticsearch启动之后再启动
    links:
      - elasticsearch:es #可以用es这个域名访问elasticsearch服务
    environment:
      - "elasticsearch.hosts=http://es:9200" #设置访问elasticsearch的地址
    ports:
      - 5601:5601
```

### 启动容器

```yaml
docker-compose up -d
```

### 查看启动情况

```yaml
docker-compose ps
```

![查看elk启动情况](https://img-blog.csdnimg.cn/ca13fa39f24c4f9488339e43d140ffa1.png)

### 相关截图

#### logstash日志展示效果
![logstash](../../images/logtash-kibana.gif)
展开后日志 source 部分
``` json
  "_source": {
    "@timestamp": "2022-05-18T13:43:34.790Z",
    "port": 55630,
    "serverName": "otlp-server",
    "namespace": "k8sNamespace_IS_UNDEFINED",
    "message": "this is tag\n",
    "@version": "1",
    "severity": "INFO",
    "thread": "http-nio-8080-exec-1",
    "pid": "3975",
    "host": "gateway",
    "class": "c.z.o.server.controller.ServerController",
    "traceId": "a7360264491f074a1b852cfcabb10fdb",
    "spanId": "e4a8f1c4606ca598",
    "podName": "podName_IS_UNDEFINED"
  },
```

通过 Logstash-logback 方式需要将 traceId 和 spanId 手动埋点。

#### otlp 日志展示效果

![logstash](../../images/otel-log-es.gif)

通过 otlp 方式输出日志，会产生很多 tag，同时也会产生 traceId 和 spanId 。

```json
"_source": {
  "@timestamp": "2022-05-18T08:39:20.661000000Z",
  "Body": "this is method3,null",
  "Resource.container.id": "7478",
  "Resource.host.arch": "amd64",
  "Resource.host.name": "cluster-ecs07",
  "Resource.os.description": "Linux 3.10.0-1160.15.2.el7.x86_64",
  "Resource.os.type": "linux",
  "Resource.process.command_line": "/usr/java/jdk1.8.0_111/jre:bin:java -javaagent:opentelemetry-javaagent-1.13.1.jar -Dotel.traces.exporter=otlp -Dotel.exporter.otlp.endpoint=http://localhost:4350 -Dotel.resource.attributes=service.name=server,username=liu -Dotel.metrics.exporter=otlp -Dotel.logs.exporter=otlp -Dotel.propagators=b3",
  "Resource.process.executable.path": "/usr/java/jdk1.8.0_111/jre:bin:java",
  "Resource.process.pid": 5728,
  "Resource.process.runtime.description": "Oracle Corporation Java HotSpot(TM) 64-Bit Server VM 25.111-b14",
  "Resource.process.runtime.name": "Java(TM) SE Runtime Environment",
  "Resource.process.runtime.version": "1.8.0_111-b14",
  "Resource.service.name": "server",
  "Resource.telemetry.auto.version": "1.13.1",
  "Resource.telemetry.sdk.language": "java",
  "Resource.telemetry.sdk.name": "opentelemetry",
  "Resource.telemetry.sdk.version": "1.13.0",
  "Resource.username": "liu",
  "SeverityNumber": 9,
  "SeverityText": "INFO",
  "SpanId": "bb890485f7b6ba05",
  "TraceFlags": 1,
  "TraceId": "b4841a6b3ec9aa93d7f002393a156ff5"
  },
```

通过 otlp 协议，实现了traceId 和 spanId 在 log 上的自动埋点。

