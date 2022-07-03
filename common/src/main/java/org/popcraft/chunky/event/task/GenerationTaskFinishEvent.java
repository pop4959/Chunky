package org.popcraft.chunky.event.task;

import org.popcraft.chunky.GenerationTask;

public class GenerationTaskFinishEvent extends GenerationTaskEvent {
    public GenerationTaskFinishEvent(final GenerationTask generationTask) {
        super(generationTask);
    }
}
