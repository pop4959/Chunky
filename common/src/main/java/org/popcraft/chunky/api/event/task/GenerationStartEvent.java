package org.popcraft.chunky.api.event.task;

import org.popcraft.chunky.event.Event;

/**
 * Event which is fired when a generation task starts or continues.
 *
 * @param world     The world identifier
 * @param shape     The selection shape
 * @param centerX   The center x coordinate
 * @param centerZ   The center z coordinate
 * @param radiusX   The primary radius (x-axis)
 * @param radiusZ   The secondary radius (z-axis)
 * @param pattern   The generation pattern
 * @param owner     The name of the player who owns the task, or null if there is none
 * @param continued If the task was continued from a previous run
 */
public record GenerationStartEvent(String world, String shape, double centerX, double centerZ, double radiusX,
                                   double radiusZ, String pattern, String owner, boolean continued) implements Event {
}
