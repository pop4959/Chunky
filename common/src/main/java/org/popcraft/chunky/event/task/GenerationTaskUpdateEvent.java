package org.popcraft.chunky.event.task;

import org.popcraft.chunky.GenerationTask;

public class GenerationTaskUpdateEvent extends GenerationTaskEvent {
    public GenerationTaskUpdateEvent(final GenerationTask generationTask) {
        super(generationTask);
    }
}
