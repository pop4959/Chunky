package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.World;

import java.util.List;
import java.util.Map;

public class StatusCommand extends ChunkyCommand {
    public StatusCommand(Chunky chunky) {
        super(chunky);
    }

    @Override
    public void execute(Sender sender, String[] args) {
        final Map<World, GenerationTask> tasks = chunky.getGenerationTasks();
        tasks.forEach(((world, generationTask) -> generationTask.getProgress().sendUpdate(sender)));
    }
}
