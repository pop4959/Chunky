package org.popcraft.chunky.util;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.api.ChunkyAPI;
import org.popcraft.chunky.event.EventBus;
import org.popcraft.chunky.event.command.ReloadCommandEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerPauseTask {
    private static final long INTERVAL = Math.max(Input.tryInteger(System.getProperty("chunky.playerPauseInterval")).orElse(3), 1);
    private final Chunky chunky;
    private final Set<String> paused = new HashSet<>();
    private final AtomicBoolean running = new AtomicBoolean();
    private volatile boolean stopped;

    public PlayerPauseTask(final Chunky chunky) {
        this.chunky = chunky;
    }

    public void register(final EventBus eventBus) {
        eventBus.subscribe(ReloadCommandEvent.class, event -> start());
        start();
    }

    public void start() {
        if (!stopped && chunky.getConfig().getPlayerPauseCount() > 0 && running.compareAndSet(false, true)) {
            schedule();
        }
    }

    public void stop() {
        stopped = true;
    }

    private void schedule() {
        CompletableFuture.runAsync(this::update, CompletableFuture.delayedExecutor(INTERVAL, TimeUnit.SECONDS)).whenComplete((result, throwable) -> {
            if (!stopped && (chunky.getConfig().getPlayerPauseCount() > 0 || !paused.isEmpty())) {
                schedule();
            } else {
                running.set(false);
                start();
            }
        });
    }

    private void update() {
        if (stopped) {
            return;
        }
        final ChunkyAPI api = chunky.getApi();
        final int playerPauseCount = chunky.getConfig().getPlayerPauseCount();
        if (playerPauseCount > 0 && chunky.getServer().getPlayers().size() >= playerPauseCount) {
            for (final String world : Set.copyOf(chunky.getGenerationTasks().keySet())) {
                if (api.pauseTask(world)) {
                    paused.add(world);
                }
            }
        } else if (!paused.isEmpty()) {
            paused.removeIf(world -> api.continueTask(world) || !chunky.getGenerationTasks().containsKey(world));
        }
    }
}
