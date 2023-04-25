package com.zy.observable;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.*;

/**
 * 线程池监控器
 */
@Service
public class ThreadPoolMonitor implements InitializingBean {

    private static final String EXECUTOR_QUERY_NAME = "book-query";
    private static final Iterable<Tag> TAG = Collections.singletonList(Tag.of("thread.pool.name", EXECUTOR_QUERY_NAME));
    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * 往ioc容器中注入一个自定义的线程池
     * 核心线程数为30
     * 最大线程数为30
     * 临时线程等待任务时间为5s
     * 阻塞队列的长度为20
     * 拒绝策略为丢弃新任务
     * @return
     */
//    public static final ThreadPoolExecutor BOOK_QUERY_THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(30,
//            30,
//            5,
//            TimeUnit.SECONDS,
//            new ArrayBlockingQueue<>(20),
//            new ThreadPoolExecutor.AbortPolicy());

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


//    public static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(30,
//            30,
//            5,
//            TimeUnit.SECONDS,
//            new ArrayBlockingQueue<>(20), new ThreadFactory() {
//
//        private final AtomicInteger counter = new AtomicInteger();
//
//        @Override
//        public Thread newThread(Runnable r) {
//            Thread thread = new Thread(r);
//            thread.setDaemon(true);
//            thread.setName(EXECUTOR_QUERY_NAME+"_" + counter.getAndIncrement());
//            return thread;
//        }
//    }, new ThreadPoolExecutor.AbortPolicy());

    private Runnable monitor = () -> {
        //这里需要捕获异常,尽管实际上不会产生异常,但是必须预防异常导致调度线程池线程失效的问题
        try {
            Metrics.gauge("thread.pool.core.size", TAG, threadPoolExecutor, ThreadPoolExecutor::getCorePoolSize);
            Metrics.gauge("thread.pool.largest.size", TAG, threadPoolExecutor, ThreadPoolExecutor::getLargestPoolSize);
            Metrics.gauge("thread.pool.max.size", TAG, threadPoolExecutor, ThreadPoolExecutor::getMaximumPoolSize);
            Metrics.gauge("thread.pool.active.size", TAG, threadPoolExecutor, ThreadPoolExecutor::getActiveCount);
            Metrics.gauge("thread.pool.thread.count", TAG, threadPoolExecutor, ThreadPoolExecutor::getPoolSize);
            // 注意如果阻塞队列使用无界队列这里不能直接取size
            Metrics.gauge("thread.pool.queue.size", TAG, threadPoolExecutor, e -> e.getQueue().size());
        } catch (Exception e) {
            //ignore
        }
    };

    @Override
    public void afterPropertiesSet() throws Exception {
        // 每1秒执行一次，采集频率
        scheduledExecutor.scheduleWithFixedDelay(monitor, 0, 1, TimeUnit.SECONDS);
    }
}

