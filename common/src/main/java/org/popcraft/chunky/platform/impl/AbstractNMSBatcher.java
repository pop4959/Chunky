package org.popcraft.chunky.platform.impl;

import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.platform.Batcher;
import org.popcraft.chunky.util.Input;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractNMSBatcher implements Batcher {
    public static final int BATCH_DIVISOR = Input.tryInteger(System.getProperty("chunky.batchDivisor")).orElse(4);

    protected final ConcurrentLinkedQueue<Runnable> ticketAddTasks = new ConcurrentLinkedQueue<>();
    protected final ArrayDeque<Runnable> futureFetchTasks = new ArrayDeque<>();
    protected final ArrayDeque<Runnable> ticketRemoveTasks = new ArrayDeque<>();
    protected final Executor ticketAddExecutor = command -> {
        if (this.shutdown) {
            this.executeSyncRaw(command);
        } else {
            this.ticketAddTasks.add(command);
            this.checkScheduleAsync();
        }
    };
    protected final Executor futureFetchExecutor = command -> {
        if (this.shutdown) {
            this.executeSyncRaw(() -> {
                this.tickTickets(); // execute because no more batches
                command.run();
            });
        } else {
            this.futureFetchTasks.add(command);
            // it is expected that tasks in ticketAddExecutor to schedule here, don't schedule again
        }
    };
    protected final Executor ticketRemoveExecutor = command -> {
        if (this.shutdown) {
            this.executeSyncRaw(command);
        } else {
            this.ticketRemoveTasks.add(command);
            this.checkScheduleSync();
        }
    };
    protected final int batchMinSize;
    protected final AtomicBoolean scheduled = new AtomicBoolean(false);
    protected volatile boolean shutdown = true;

    public AbstractNMSBatcher(int maxWorkingCount) {
        this.batchMinSize = maxWorkingCount / BATCH_DIVISOR;
    }

    public AbstractNMSBatcher() {
        this(GenerationTask.MAX_WORKING_COUNT);
    }

    protected abstract void tickTickets();

    protected abstract void executeSyncRaw(Runnable command);

    protected void runImpl() {
        boolean wasShutdown = this.shutdown;
        try {
            drainQueue(this.ticketAddTasks);
            drainQueue(this.ticketRemoveTasks);
            this.tickTickets();
            drainQueue(this.futureFetchTasks);
        } finally {
            this.scheduled.set(false);
        }
        this.checkScheduleAsync();
        this.checkScheduleSync();
        if (wasShutdown ^ this.shutdown) {
            this.schedule(); // flush the last raced tasks
        }
    }

    private void schedule() {
        if (this.scheduled.compareAndSet(false, true)) {
            this.executeSyncRaw(this::runImpl);
        }
    }

    private void checkScheduleAsync() {
        if (this.ticketAddTasks.size() >= this.batchMinSize) {
            this.schedule();
        }
    }

    private void checkScheduleSync() {
        if (this.ticketRemoveTasks.size() >= this.batchMinSize) { // no futureFetchTasks check, see comments above
            this.schedule();
        }
    }

    @Override
    public void shutdown() {
        if (this.shutdown) {
            throw new IllegalStateException("Batcher is already shutdown");
        }
        this.shutdown = true;
        this.schedule();
    }

    @Override
    public void resume() {
        if (!this.shutdown) {
            throw new IllegalStateException("Batcher is running");
        }
        this.shutdown = false;
    }

    public Executor getTicketAddExecutor() {
        return this.ticketAddExecutor;
    }

    public Executor getFutureFetchExecutor() {
        return this.futureFetchExecutor;
    }

    public Executor getTicketRemoveExecutor() {
        return this.ticketRemoveExecutor;
    }

    protected static void drainQueue(Queue<Runnable> queue) {
        Runnable r;
        while ((r = queue.poll()) != null) {
            try {
                r.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
