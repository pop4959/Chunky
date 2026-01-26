package org.popcraft.chunky.platform.impl.batcher;

import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.platform.Batcher;
import org.popcraft.chunky.util.Input;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractBatcher implements Batcher {
    public static final int BATCH_DIVISOR = Input.tryInteger(System.getProperty("chunky.batchDivisor")).orElse(4);
    protected final ConcurrentLinkedQueue<Runnable> ticketAddTasks = new ConcurrentLinkedQueue<>();
    protected final ConcurrentLinkedQueue<Runnable> ticketRemoveTasks = new ConcurrentLinkedQueue<>();
    protected final ConcurrentLinkedQueue<Runnable> chunkLoadTasks = new ConcurrentLinkedQueue<>();
    protected final Executor ticketAddExecutor = command -> {
        if (this.shutdown.get()) {
            this.runSync(command);
        } else {
            this.ticketAddTasks.add(command);
            this.scheduleIfReady();
        }
    };
    protected final Executor ticketRemoveExecutor = command -> {
        if (this.shutdown.get()) {
            this.runSync(command);
        } else {
            this.ticketRemoveTasks.add(command);
            this.scheduleIfReady();
        }
    };
    protected final Executor chunkLoadExecutor = command -> {
        if (this.shutdown.get()) {
            this.runSync(() -> {
                this.tickTickets();
                command.run();
            });
        } else {
            this.chunkLoadTasks.add(command);
        }
    };
    protected final int batchSize = Math.max(1, GenerationTask.MAX_WORKING_COUNT / BATCH_DIVISOR);
    protected final AtomicBoolean scheduled = new AtomicBoolean(false);
    protected final AtomicBoolean shutdown = new AtomicBoolean(true);

    protected abstract void tickTickets();

    protected abstract void runSync(Runnable command);

    @Override
    public void shutdown() {
        if (this.shutdown.get()) {
            throw new IllegalStateException("Batcher is already shutdown");
        }
        this.shutdown.set(true);
        this.schedule();
    }

    @Override
    public void resume() {
        if (!this.shutdown.get()) {
            throw new IllegalStateException("Batcher is running");
        }
        this.shutdown.set(false);
    }

    public Executor getTicketAddExecutor() {
        return this.ticketAddExecutor;
    }

    public Executor getTicketRemoveExecutor() {
        return this.ticketRemoveExecutor;
    }

    public Executor getChunkLoadExecutor() {
        return this.chunkLoadExecutor;
    }

    private void processTaskQueue() {
        final boolean wasShutdown = this.shutdown.get();
        try {
            runTasks(this.ticketAddTasks);
            runTasks(this.ticketRemoveTasks);
            this.tickTickets();
            runTasks(this.chunkLoadTasks);
        } finally {
            this.scheduled.set(false);
        }
        this.scheduleIfReady();
        if (!wasShutdown && this.shutdown.get()) {
            this.schedule();
        }
    }

    private void runTasks(final Queue<Runnable> queue) {
        Runnable r;
        while ((r = queue.poll()) != null) {
            r.run();
        }
    }

    private void schedule() {
        if (this.scheduled.compareAndSet(false, true)) {
            this.runSync(this::processTaskQueue);
        }
    }

    private void scheduleIfReady() {
        if (this.ticketRemoveTasks.size() >= this.batchSize || this.ticketAddTasks.size() >= this.batchSize) {
            this.schedule();
        }
    }
}
