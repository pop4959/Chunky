package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.task.GenerationTask;
import org.popcraft.chunky.task.TaskManager;

public class PauseCommand extends ChunkyCommand {
    public PauseCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(CommandSender sender, String[] args) {
        TaskManager taskManager = chunky.getTaskManager();
        if (chunky.getTaskManager().getTasks().size() == 0) {
            sender.sendMessage(chunky.message("format_pause_no_tasks", chunky.message("prefix")));
            return;
        }
        for (GenerationTask task : taskManager.getTasks()) {
            sender.sendMessage(chunky.message("format_pause", chunky.message("prefix"), task.getWorld().getName()));
        }
        chunky.getTaskManager().stopAll(false, false, false);
    }
}
