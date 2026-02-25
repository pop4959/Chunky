package org.popcraft.chunky.util;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskScheduler {
    /** True when running on Java 21+, which supports virtual threads. */
    private static final boolean VIRTUAL_THREADS_AVAILABLE = Runtime.version().feature() >= 21;

    private final ExecutorService executor;
    private final Set<Future<?>> futures = ConcurrentHashMap.newKeySet();

    public TaskScheduler() {
        this.executor = buildExecutor();
    }

    private static ExecutorService buildExecutor() {
        if (VIRTUAL_THREADS_AVAILABLE) {
            // Java 21+: use an unbounded virtual-thread-per-task executor.
            // Virtual threads park with near-zero overhead, so each GenerationTask feeder
            // can block on LockSupport.parkNanos without occupying a platform thread.
            try {
                // Reflectively call Executors.newVirtualThreadPerTaskExecutor() to stay source-compatible
                // with Java 17 (the project's minimum compile target).
                final Method newVTPTE = Executors.class.getMethod("newVirtualThreadPerTaskExecutor");
                return (ExecutorService) newVTPTE.invoke(null);
            } catch (Exception ignored) {
                // Reflection failed — fall through to platform thread pool.
            }
        }
        // Java 17/18/19 or reflection failure: use a cached platform thread pool.
        final ThreadPoolExecutor pool = new ThreadPoolExecutor(
                3, Integer.MAX_VALUE, 5, TimeUnit.MINUTES, new SynchronousQueue<>()
        );
        pool.setThreadFactory(buildDaemonFactory());
        pool.prestartAllCoreThreads();
        pool.allowCoreThreadTimeOut(true);
        return pool;
    }

    private static ThreadFactory buildDaemonFactory() {
        return runnable -> {
            final Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        };
    }

    public void runTask(final Runnable runnable) {
        futures.add(executor.submit(runnable));
        futures.removeIf(Future::isDone);
    }

    public void cancelTasks() {
        for (final Future<?> future : futures) {
            future.cancel(true);
        }
        futures.clear();
    }

    public static boolean isVirtualThreadsAvailable() {
        return VIRTUAL_THREADS_AVAILABLE;
    }
}
