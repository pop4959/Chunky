package org.popcraft.chunky.api.event.task;

import org.popcraft.chunky.event.Event;

public record GenerationCompleteEvent(String world) implements Event {
}
