package org.popcraft.chunky.api;

import org.popcraft.chunky.api.event.task.GenerationCompleteEvent;
import org.popcraft.chunky.api.event.task.GenerationProgressEvent;

import java.util.function.Consumer;

public interface ChunkyAPI {
    int version();

    boolean isRunning(final String world);

    boolean startTask(final String world, final String shape, final double centerX, final double centerZ, final double radiusX, final double radiusZ, final String pattern);

    boolean pauseTask(final String world);

    boolean continueTask(final String world);

    boolean cancelTask(final String world);

    void onGenerationProgress(final Consumer<GenerationProgressEvent> event);

    void onGenerationComplete(final Consumer<GenerationCompleteEvent> event);
}
