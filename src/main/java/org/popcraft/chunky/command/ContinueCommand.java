package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.task.TaskManager;

public class ContinueCommand extends ChunkyCommand {
    public ContinueCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(CommandSender sender, String[] args) {
        final TaskManager taskManager = chunky.getTaskManager();
        chunky.getConfigStorage().loadTasks().forEach(generationTask -> {
            if (!taskManager.isRunning(generationTask.getWorld())) {
                taskManager.start(generationTask.getWorld(), generationTask);
                sender.sendMessage(chunky.message("format_continue", chunky.message("prefix"), generationTask.getWorld().getName()));
            } else {
                sender.sendMessage(chunky.message("format_started_already", chunky.message("prefix"), generationTask.getWorld().getName()));
            }
        });
    }
}
