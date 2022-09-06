package org.popcraft.chunky.api;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.api.event.task.GenerationCompleteEvent;
import org.popcraft.chunky.api.event.task.GenerationProgressEvent;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.Parameter;

import java.util.function.Consumer;

@SuppressWarnings("ClassCanBeRecord")
public class ChunkyAPIImpl implements ChunkyAPI {
    private final Chunky chunky;

    public ChunkyAPIImpl(final Chunky chunky) {
        this.chunky = chunky;
    }

    @Override
    public int version() {
        return 0;
    }

    @Override
    public boolean isRunning(final String world) {
        return chunky.getGenerationTasks().containsKey(world);
    }

    @Override
    public boolean startTask(final String world, final String shape, final double centerX, final double centerZ, final double radiusX, final double radiusZ, final String pattern) {
        final World implWorld = Input.tryWorld(chunky, world).orElse(null);
        if (implWorld == null) {
            return false;
        }
        if (chunky.getGenerationTasks().containsKey(world)) {
            return false;
        }
        final Selection selection = Selection.builder(chunky, implWorld)
                .shape(shape).center(centerX, centerZ)
                .radiusX(radiusX).radiusZ(radiusZ)
                .pattern(Parameter.of(pattern)).build();
        final GenerationTask task = new GenerationTask(chunky, selection);
        chunky.getGenerationTasks().put(world, task);
        chunky.getScheduler().runTask(task);
        return true;
    }

    @Override
    public boolean pauseTask(final String world) {
        final GenerationTask task = chunky.getGenerationTasks().get(world);
        if (task == null) {
            return false;
        }
        task.stop(false);
        return true;
    }

    @Override
    public boolean continueTask(final String world) {
        final World implWorld = Input.tryWorld(chunky, world).orElse(null);
        if (implWorld == null) {
            return false;
        }
        final GenerationTask task = chunky.getTaskLoader().loadTask(implWorld).orElse(null);
        if (task == null || task.isCancelled()) {
            return false;
        }
        if (chunky.getGenerationTasks().containsKey(world)) {
            return false;
        }
        chunky.getGenerationTasks().put(world, task);
        chunky.getScheduler().runTask(task);
        return true;
    }

    @Override
    public boolean cancelTask(final String world) {
        final World implWorld = Input.tryWorld(chunky, world).orElse(null);
        if (implWorld == null) {
            return false;
        }
        if (!chunky.getGenerationTasks().containsKey(world)) {
            return false;
        }
        chunky.getGenerationTasks().remove(world).stop(true);
        chunky.getTaskLoader().cancelTask(implWorld);
        return true;
    }

    @Override
    public void onGenerationProgress(final Consumer<GenerationProgressEvent> event) {
        chunky.getEventBus().subscribe(GenerationProgressEvent.class, event);
    }

    @Override
    public void onGenerationComplete(final Consumer<GenerationCompleteEvent> event) {
        chunky.getEventBus().subscribe(GenerationCompleteEvent.class, event);
    }
}
