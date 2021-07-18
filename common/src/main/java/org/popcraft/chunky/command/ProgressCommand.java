package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.World;

import java.util.Map;

public class ProgressCommand extends ChunkyCommand {
    public ProgressCommand(Chunky chunky) {
        super(chunky);
    }

    @Override
    public void execute(Sender sender, String[] args) {
        final Map<World, GenerationTask> generationTasks = chunky.getGenerationTasks();
        if (generationTasks.isEmpty()) {
            sender.sendMessagePrefixed("format_progress_no_tasks");
            return;
        }
        for (World world : chunky.getServer().getWorlds()) {
            if (generationTasks.containsKey(world)) {
                generationTasks.get(world).getProgress().sendUpdate(sender);
            }
        }
    }
}
