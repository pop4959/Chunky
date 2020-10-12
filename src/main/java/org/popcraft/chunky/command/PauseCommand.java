package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;

public class PauseCommand extends ChunkyCommand {
    public PauseCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(CommandSender sender, String[] args) {
        if (chunky.getGenerationTasks().size() == 0) {
            sender.sendMessage(chunky.message("format_pause_no_tasks", chunky.message("prefix")));
            return;
        }
        for (GenerationTask generationTask : chunky.getGenerationTasks().values()) {
            generationTask.stop(false);
            sender.sendMessage(chunky.message("format_pause", chunky.message("prefix"), generationTask.getWorld().getName()));
        }
    }
}
