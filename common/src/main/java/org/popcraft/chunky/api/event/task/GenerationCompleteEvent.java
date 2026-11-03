package org.popcraft.chunky.api.event.task;

import org.popcraft.chunky.event.Event;

/**
 * Event which is fired when a generation task completes, is paused, or is cancelled.
 *
 * @param world   The world identifier associated with the completed task
 * @param shape   The selection shape
 * @param centerX The center x coordinate
 * @param centerZ The center z coordinate
 * @param radiusX The primary radius (x-axis)
 * @param radiusZ The secondary radius (z-axis)
 * @param pattern The generation pattern
 * @param owner   The name of the player who owns the task, or null if there is none
 * @param chunks  The number of chunks processed
 * @param time    The total time spent on the task in milliseconds
 * @param reason  The reason the task stopped
 */
public record GenerationCompleteEvent(String world, String shape, double centerX, double centerZ, double radiusX,
                                      double radiusZ, String pattern, String owner, long chunks, long time,
                                      Reason reason) implements Event {
    /**
     * The reason a generation task stopped.
     */
    public enum Reason {
        COMPLETED,
        PAUSED,
        CANCELLED
    }
}
