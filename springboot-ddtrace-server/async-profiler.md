async-profiler 相关概念和使用，参考 [profiling](../profiling/README.md)

`ProfilingController` 提供了2个 API.

- GET generator（http://localhost:8086/profiling/generator）: 用于生成文件  
- GET mapReader（http://localhost:8086/profiling/mapReader）: 用于解析文件并存储到map中。  


执行 async-profiler 语句：

> ./profiler.sh -e alloc -o flamegraph=total -d 10 -f out.html jps

如果需要将结果上传到观测云进行分析，可以使用以下命令

> DATAKIT_URL=http://localhost:9529 APP_ENV=test APP_VERSION=1.0.0 PROFILING_EVENT=cpu,alloc,lock PROFILING_DURATION=10 PROCESS_ID=`ps -ef |grep java|grep springboot|grep -v grep|awk '{print $2}'` SERVICE_NAME=ddtrace-server bash collect.sh

