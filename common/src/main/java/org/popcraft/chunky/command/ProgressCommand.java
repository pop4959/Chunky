package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.util.TranslationKey;

import java.util.Map;

public class ProgressCommand extends ChunkyCommand {
    public ProgressCommand(final Chunky chunky) {
        super(chunky);
    }

    @Override
    public void execute(final Sender sender, final String[] args) {
        final Map<String, GenerationTask> generationTasks = chunky.getGenerationTasks();
        if (generationTasks.isEmpty()) {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_PROGRESS_NO_TASKS);
            return;
        }
        for (World world : chunky.getServer().getWorlds()) {
            if (generationTasks.containsKey(world.getName())) {
                generationTasks.get(world.getName()).getProgress().sendUpdate(sender);
            }
        }
    }
}
