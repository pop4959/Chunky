package org.popcraft.chunky.util;

import java.util.Optional;

public final class MaxWorkingCount {
    private static final int DEFAULT = 64;
    private static final Optional<Integer> PROPERTY = Input.tryInteger(System.getProperty("chunky.maxWorkingCount"));

    private MaxWorkingCount() {
    }

    public static int get() {
        if (PROPERTY.isPresent()) {
            return Math.max(1, PROPERTY.get());
        }
        final Optional<Integer> moonriseWorkerThreads = Reflection.getStaticField("ca.spottedleaf.moonrise.common.util.MoonriseCommon", "WORKER_POOL")
                .flatMap(workerPool -> Reflection.invokeMethod(workerPool, "getCoreThreads"))
                .filter(Thread[].class::isInstance)
                .map(coreThreads -> ((Thread[]) coreThreads).length);
        if (moonriseWorkerThreads.isPresent()) {
            return Math.clamp(moonriseWorkerThreads.get() * 8L, DEFAULT, 512);
        }
        final Optional<Integer> c2meParallelism = Reflection.getStaticField("com.ishland.c2me.base.common.GlobalExecutors", "GLOBAL_EXECUTOR_PARALLELISM")
                .filter(Integer.class::isInstance)
                .map(Integer.class::cast);
        if (c2meParallelism.isPresent()) {
            return Math.clamp(c2meParallelism.get() * 8L, DEFAULT, 1024);
        }
        return DEFAULT;
    }
}
