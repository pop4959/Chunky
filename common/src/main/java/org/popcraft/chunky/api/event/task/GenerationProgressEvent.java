package org.popcraft.chunky.api.event.task;

import org.popcraft.chunky.event.Event;

public record GenerationProgressEvent(String world, long chunks, boolean complete, float progress, long hours,
                                      long minutes, long seconds, double rate, long x, long z) implements Event {
}
