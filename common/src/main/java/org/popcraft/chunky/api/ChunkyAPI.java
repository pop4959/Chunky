package org.popcraft.chunky.api;

import org.popcraft.chunky.api.event.task.GenerationCompleteEvent;
import org.popcraft.chunky.api.event.task.GenerationProgressEvent;

import java.util.function.Consumer;

/**
 * The Chunky API
 */
@SuppressWarnings("unused")
public interface ChunkyAPI {
    /**
     * Gets the current API version.
     *
     * @return The api version
     */
    int version();

    /**
     * Gets whether a generation task is currently running for a world.
     *
     * @param world The world identifier
     * @return If a task is running in that world
     */
    boolean isRunning(final String world);

    /**
     * Starts a generation task with a given selection in a world.
     *
     * @param world   The world identifier
     * @param shape   The selection shape
     * @param centerX The center x coordinate
     * @param centerZ The center z coordinate
     * @param radiusX The primary radius (x-axis)
     * @param radiusZ The secondary radius (z-axis) (only used for certain shapes)
     * @param pattern The generation pattern
     * @return If the task was created and started successfully
     */
    boolean startTask(final String world, final String shape, final double centerX, final double centerZ, final double radiusX, final double radiusZ, final String pattern);

    /**
     * Pauses a generation task in a world.
     *
     * @param world The world identifier
     * @return If the task was paused
     */
    boolean pauseTask(final String world);

    /**
     * Continues a generation task in a world.
     *
     * @param world The world identifier
     * @return If the task was continued
     */
    boolean continueTask(final String world);

    /**
     * Cancels a generation task in a world.
     *
     * @param world The world identifier
     * @return If the task was cancelled
     */
    boolean cancelTask(final String world);

    /**
     * Register a listener for generation progress events.
     *
     * @param listener The listener
     */
    void onGenerationProgress(final Consumer<GenerationProgressEvent> listener);

    /**
     * Register a listener for generation complete events.
     *
     * @param listener The listener
     */
    void onGenerationComplete(final Consumer<GenerationCompleteEvent> listener);
}
