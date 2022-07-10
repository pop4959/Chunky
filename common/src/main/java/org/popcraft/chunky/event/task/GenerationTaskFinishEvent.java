package org.popcraft.chunky.event.task;

import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.event.Event;

public record GenerationTaskFinishEvent(GenerationTask generationTask) implements Event {
}
