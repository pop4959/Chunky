package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.platform.Sender;

import java.util.List;

/**
 * @author Jamalam360
 */
public class StatusCommand extends ChunkyCommand {
    public StatusCommand(Chunky chunky) {
        super(chunky);
    }

    @Override
    public void execute(Sender sender, String[] args) {
        final List<GenerationTask> tasks = chunky.getConfig().loadTasks();
        tasks.forEach((generationTask -> generationTask.getProgress().sendUpdate(sender)));
    }
}
