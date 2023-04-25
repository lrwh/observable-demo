# 利用 Micrometer 监控线程池指标

首先我们要整理一下`ThreadPoolExecutor`中提供的度量数据项和`micrometer`对应的 Tag 的映射关系：

1. 线程池名称，Tag：`thread.pool.name`，这个很重要，用于区分各个线程池的数据，如果使用 IOC 容器管理，可以使用 BeanName 代替。
1. `int getCorePoolSize()`：核心线程数，Tag：`thread.pool.core.size`。
1. `int getLargestPoolSize()`：历史峰值线程数，Tag：`thread.pool.largest.size`。
1. `int getMaximumPoolSize()`：最大线程数(线程池线程容量)，Tag：`thread.pool.max.size`。
1. `int getActiveCount()`：当前活跃线程数，Tag：`thread.pool.active.size`。
1. `int getPoolSize()`：当前线程池中运行的线程总数(包括核心线程和非核心线程)，Tag：`thread.pool.thread.count`。
1. 当前任务队列中积压任务的总数，Tag：`thread.pool.queue.size`，这个需要动态计算得出。

默认创建线程池方式

```java
    /**
     * 往ioc容器中注入一个自定义的线程池
     * 核心线程数为30
     * 最大线程数为30
     * 临时线程等待任务时间为5s
     * 阻塞队列的长度为20
     * 拒绝策略为丢弃新任务
     * @return
     */
    public static final ThreadPoolExecutor BOOK_QUERY_THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(30,
            30,
            5,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(20),
            new ThreadPoolExecutor.AbortPolicy());
```

两种配置线程池名称的方式

方式一：重写 `beforeExecute`

```java
    /**
     * 往ioc容器中注入一个自定义的线程池
     * 核心线程数为30
     * 最大线程数为30
     * 临时线程等待任务时间为5s
     * 阻塞队列的长度为20
     * 拒绝策略为丢弃新任务
     * @return
     */
    public static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(30,
            30,
            5,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(20),
            new ThreadPoolExecutor.AbortPolicy()){
        protected void beforeExecute(Thread t, Runnable r) {
            //设置线程池名称
            t.setName(EXECUTOR_QUERY_NAME);
        }

        protected void afterExecute(Runnable r, Throwable t) {
            Thread.currentThread().setName("");
        }

    };
```

方式二：采用 `ThreadFactory` ，重写 `newThread`

```java
    /**
     * 往ioc容器中注入一个自定义的线程池
     * 核心线程数为30
     * 最大线程数为30
     * 临时线程等待任务时间为5s
     * 阻塞队列的长度为20
     * 拒绝策略为丢弃新任务
     * @return
     */
    public static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(30,
            30,
            5,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(20), new ThreadFactory() {

        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(EXECUTOR_QUERY_NAME+"_" + counter.getAndIncrement());
            return thread;
        }
    }, new ThreadPoolExecutor.AbortPolicy());
```

`thread`部分指标信息如下：

```shell
liurui@liurui:~$ curl http://localhost:8091/actuator/prometheus |grep thread
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100 # HELP thread_pool_active_size   0      0 --:--:-- --:--:-- --:--:--     0
1# TYPE thread_pool_active_size gauge
1thread_pool_active_size{application="demo",env="dev",thread_pool_name="book-query",} 5.0
107  1# HELP thread_pool_thread_count  
0# TYPE thread_pool_thread_count gauge
0thread_pool_thread_count{application="demo",env="dev",thread_pool_name="book-query",} 30.0
 # HELP thread_pool_queue_size  
1# TYPE thread_pool_queue_size gauge
1thread_pool_queue_size{application="demo",env="dev",thread_pool_name="book-query",} 0.0
1# HELP thread_pool_max_size  
0# TYPE thread_pool_max_size gauge
7thread_pool_max_size{application="demo",env="dev",thread_pool_name="book-query",} 30.0
    0# HELP thread_pool_largest_size  
 # TYPE thread_pool_largest_size gauge
  thread_pool_largest_size{application="demo",env="dev",thread_pool_name="book-query",} 30.0
  # HELP jvm_threads_peak_threads The peak live thread count since the Java virtual machine started or peak was reset
0 # TYPE jvm_threads_peak_threads gauge
 1jvm_threads_peak_threads{application="demo",env="dev",} 95.0
5# HELP thread_pool_core_size  
4# TYPE thread_pool_core_size gauge
9k  thread_pool_core_size{application="demo",env="dev",thread_pool_name="book-query",} 30.0
    0# HELP jvm_threads_states_threads The current number of threads having NEW state
 # TYPE jvm_threads_states_threads gauge
-jvm_threads_states_threads{application="demo",env="dev",state="timed-waiting",} 39.0
-:jvm_threads_states_threads{application="demo",env="dev",state="terminated",} 0.0
--:-- --:--:-- --:--:-- 1549k
jvm_threads_states_threads{application="demo",env="dev",state="new",} 0.0
jvm_threads_states_threads{application="demo",env="dev",state="blocked",} 0.0
jvm_threads_states_threads{application="demo",env="dev",state="waiting",} 38.0
jvm_threads_states_threads{application="demo",env="dev",state="runnable",} 18.0
# HELP jvm_threads_live_threads The current number of live threads including both daemon and non-daemon threads
# TYPE jvm_threads_live_threads gauge
jvm_threads_live_threads{application="demo",env="dev",} 95.0
# HELP jvm_threads_daemon_threads The current number of live daemon threads
# TYPE jvm_threads_daemon_threads gauge
jvm_threads_daemon_threads{application="demo",env="dev",} 56.0

```

