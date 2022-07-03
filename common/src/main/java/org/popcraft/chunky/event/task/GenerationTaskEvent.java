package org.popcraft.chunky.event.task;

import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.event.Event;

public class GenerationTaskEvent implements Event {
    private final GenerationTask generationTask;

    public GenerationTaskEvent(final GenerationTask generationTask) {
        this.generationTask = generationTask;
    }

    public GenerationTask getGenerationTask() {
        return generationTask;
    }
}
