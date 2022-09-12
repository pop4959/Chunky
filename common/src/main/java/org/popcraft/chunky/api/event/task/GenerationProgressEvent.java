package org.popcraft.chunky.api.event.task;

import org.popcraft.chunky.event.Event;

/**
 * Event which is fired when a generation task calculates progress.
 *
 * @param world    The world identifier
 * @param chunks   The number of chunks generated
 * @param complete If the generation task completed
 * @param progress The percent progress
 * @param hours    The number of hours elapsed for this task
 * @param minutes  The number of minutes elapsed for this task
 * @param seconds  The number of seconds elapsed for this task
 * @param rate     The current average generation rate
 * @param x        The current chunk's x coordinate
 * @param z        The current chunk's z coordinate
 */
public record GenerationProgressEvent(String world, long chunks, boolean complete, float progress, long hours,
                                      long minutes, long seconds, double rate, long x, long z) implements Event {
}
