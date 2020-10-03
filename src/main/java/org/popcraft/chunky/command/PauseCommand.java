package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;

public class PauseCommand extends ChunkyCommand {
    public PauseCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(CommandSender sender, String[] args) {
        for (GenerationTask generationTask : chunky.getGenerationTasks().values()) {
            generationTask.stop(false, false);
            sender.sendMessage(chunky.message("format_pause", chunky.message("prefix"), generationTask.getWorld().getName()));
        }
    }
}
