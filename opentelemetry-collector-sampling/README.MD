# OpenTelemetry 采样



已发表博客，地址 [https://blog.csdn.net/liurui_wuhan/article/details/125043307](https://blog.csdn.net/liurui_wuhan/article/details/125043307)



## [概率采样处理器](https://github.com/open-telemetry/opentelemetry-collector-contrib/blob/main/processor/probabilisticsamplerprocessor)

顾名思义，概率采样器，是按照某种概率进行采样。同样OpenTelemetry 支持两种概率采样

> 1、`sampling.priority` OpenTracing 定义的语义约定
> 
> 2、TraceId hash

`sampling.priority`语义约定优先于 TraceId hash。顾名思义，TraceId hash 采样基于由TraceId 确定的 hash 值。为了使 TraceId hash 起作用，给定层的所有收集器（例如，在同一负载均衡器后面）必须具有相同的`hash_seed`. 也可以利用`hash_seed`不同收集器层的不同来支持额外的采样要求。有关配置规范，请参阅 [config.go](https://github.com/open-telemetry/opentelemetry-collector-contrib/blob/main/processor/probabilisticsamplerprocessor/config.go)。

可以修改以下配置选项：

- `hash_seed`（无默认值）：用于计算哈希算法的整数。请注意，给定层的所有收集器（例如，在同一个负载均衡器后面）应该具有相同的 hash_seed。
  
  在使用多层采集器实现所需采样率的情况下，这一点很重要，例如：第一层为10%，第二层为10%，总体采样率为1%（10%x 10%）。
  
  如果所有层使用相同的种子，则通过一层的所有数据也将通过下一层，与配置的采样率无关。在不同的层中有不同的种子可以确保每层中的采样率按预期工作。

- `sampling_percentage`（默认值 = 0）：对 trace 进行采样的百分比；>= 100 表示采集所有的 trace。

配置概率采样器

```toml
processors:
  # 概率采样器
  probabilistic_sampler:
    hash_seed: 22
    sampling_percentage: 15.3
```

启用概率采样器

```toml
service:
  extensions: [pprof, zpages, health_check]
  pipelines:
    traces:
      receivers: [otlp]
      processors: [batch,probabilistic_sampler]
      exporters: [otlp]
    metrics:
      receivers: [otlp]
      processors: [batch]
      exporters: [otlp]
```

## [尾部采样器](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/tailsamplingprocessor)

尾部采样处理器根据一组定义的策略对 trace 进行采样。目前，该处理器仅适用于 collector 的单个实例。从技术上讲，traceId 感知负载平衡可用于支持多个 collector 实例，但此配置尚未经过测试。有关配置规范，请参阅 [config.go](https://github.com/open-telemetry/opentelemetry-collector-contrib/blob/main/processor/tailsamplingprocessor/config.go)。

需要以下配置选项：

- `policies`（无默认值）：用于做出抽样决策的策略

目前支持多种策略。这些包括：

> - `always_sample`：采样所有的 traces
> - `latency`：基于 trace 持续时间的采样。持续时间是通过查看最早的开始时间和最晚的结束时间来确定的，而不考虑两者之间发生的事情。
> - `numeric_attribute`: 基于数字属性的采样
> - `probabilistic`：采样一定百分比的 trace 。
> - `status_code`: 基于状态码的示例 ( `OK`,`ERROR`或`UNSET`)。很多人当做是response body json的 code（现在很多项目都这么定义），其实是不对的。
> - `string_attribute`：基于字符串属性值匹配的采样，支持精确匹配和正则表达式值匹配
> - `rate_limiting`: 基于速率的采样
> - `and`：基于多个策略的采样，创建一个 AND 策略
> - `composite`：基于上述采样器组合的采样，每个采样器具有排序和速率分配。费率分配为每个保单订单分配一定百分比的跨度。例如，如果我们将 max_total_spans_per_second 设置为 100，那么我们可以将 rate_allocation 设置如下
>   1. test-composite-policy-1 = max_total_spans_per_second 的 50 % = 50 spans_per_second
>   2. test-composite-policy-2 = max_total_spans_per_second 的 25 % = 25 spans_per_second
>   3. 为确保剩余容量被填满，请使用 always_sample 作为策略之一
> 
> 还可以修改以下配置选项：
> 
> - `decision_wait`（默认 = 30 秒）：在做出采样决定之前，从 trace 的第一个跨度开始的等待时间
> - `num_traces`（默认值 = 50000）：内存中保存的 trace 数
> - `expected_new_traces_per_sec`（默认 = 0）：预期的新 trace 数（有助于分配数据结构）

例如：

```toml
processors:
  tail_sampling:
    decision_wait: 10s
    num_traces: 100
    expected_new_traces_per_sec: 10
    policies:      [
          {
            name: test-policy-1,
            type: always_sample
          },
          {
            name: test-policy-2,
            type: latency,
            latency: {threshold_ms: 5000}
          },
          {
            name: test-policy-3,
            type: numeric_attribute,
            numeric_attribute: {key: key1, min_value: 50, max_value: 100}
          },
          {
            name: test-policy-4,
            type: probabilistic,
            probabilistic: {sampling_percentage: 10}
          },
          {
            name: test-policy-5,
            type: status_code,
            status_code: {status_codes: [ERROR, UNSET]}
          },
          {
            name: test-policy-6,
            type: string_attribute,
            string_attribute: {key: key2, values: [value1, value2]}
          },
          {
            name: test-policy-7,
            type: string_attribute,
            string_attribute: {key: key2, values: [value1, val*], enabled_regex_matching: true, cache_max_size: 10}
          },
          {
            name: test-policy-8,
            type: rate_limiting,
            rate_limiting: {spans_per_second: 35}
         },
         {
            name: test-policy-9,
            type: string_attribute,
            string_attribute: {key: http.url, values: [\/health, \/metrics], enabled_regex_matching: true, invert_match: true}
         },
         {
            name: and-policy-1,
            type: and,
            and: {
              and_sub_policy:               [
                {
                  name: test-and-policy-1,
                  type: numeric_attribute,
                  numeric_attribute: { key: key1, min_value: 50, max_value: 100 }
                },
                {
                    name: test-and-policy-2,
                    type: string_attribute,
                    string_attribute: { key: key2, values: [ value1, value2 ] }
                },
              ]
            }
         },
         {
            name: composite-policy-1,
            type: composite,
            composite:              {
                max_total_spans_per_second: 1000,
                policy_order: [test-composite-policy-1, test-composite-policy-2, test-composite-policy-3],
                composite_sub_policy:                  [
                    {
                      name: test-composite-policy-1,
                      type: numeric_attribute,
                      numeric_attribute: {key: key1, min_value: 50, max_value: 100}
                    },
                    {
                      name: test-composite-policy-2,
                      type: string_attribute,
                      string_attribute: {key: key2, values: [value1, value2]}
                    },
                    {
                      name: test-composite-policy-3,
                      type: always_sample
                    }
                  ],
                rate_allocation:                  [
                    {
                      policy: test-composite-policy-1,
                      percent: 50
                    },
                    {
                      policy: test-composite-policy-2,
                      percent: 25
                    }
                  ]
              }
          },
        ]
```

## 服务列表

| 服务名称              | 端口        | 描述                                                                                                                                                                             |
| ----------------- | --------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| otel-collector    |           | otel/opentelemetry-collector-contrib:0.51.0                                                                                                                                    |
| springboot_server | 8080:8080 | opentelemetry-agent 版本 1.13.1， 源码地址：[https://github.com/lrwh/observable-demo/tree/main/springboot-server](https://github.com/lrwh/observable-demo/tree/main/springboot-server) |

启动服务

```shell
docker-compose up -d
```

以下示例主要以尾部采样策略为测试场景。

配置尾部采样器

```toml
processors:
  # 尾部采样器
  tail_sampling:
    decision_wait: 10s
    num_traces: 100
    expected_new_traces_per_sec: 100
    policies:
      [
        {
          name: policy-1,
          type: status_code,
          status_code: {status_codes: [ERROR]}
        },
        {
          name: policy-2,
          type: probabilistic,
          probabilistic: {sampling_percentage: 20}
        }
      ]
```

启用尾部采样器

```toml
service:
  extensions: [pprof, zpages, health_check]
  pipelines:
    traces:
      receivers: [otlp]
      processors: [batch,tail_sampling]
      exporters: [otlp]
    metrics:
      receivers: [otlp]
      processors: [batch]
      exporters: [otlp]
```

## 场景一

1、访问服务API `gateway` 5 次，每次均为正常返回

> curl http://localhost:8080/gateway

2、前往观测云查看trace信息，按照采样规则，最多采样一次trace数据。

## 场景二

1、设置启动客户端

> curl http://localhost:8080/setClient?c=true

当前演示demo并没有启动客户端服务，所以后续调用 `gateway` 接口会有异常产生。

2、访问 5 次`gateway`服务,返回 `{"msg":"client 调用失败","code":500}`

3、前往观测云查看trace信息，按照采样规则，如果发生异常，则异常全采。
