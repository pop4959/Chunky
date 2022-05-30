package org.popcraft.chunky.event.task;

import org.popcraft.chunky.GenerationTask;

public class GenerationTaskFinishEvent extends GenerationTaskEvent {
    public GenerationTaskFinishEvent(GenerationTask generationTask) {
        super(generationTask);
    }
}
