package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.task.GenerationTask;
import org.popcraft.chunky.task.TaskManager;

import java.util.List;

public class ContinueCommand extends ChunkyCommand {
    public ContinueCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(CommandSender sender, String[] args) {
        final List<GenerationTask> loadTasks = chunky.getConfigStorage().loadTasks();
        if (loadTasks.size() == 0) {
            sender.sendMessage(chunky.message("format_continue_no_tasks", chunky.message("prefix")));
            return;
        }
        final TaskManager taskManager = chunky.getTaskManager();
        loadTasks.forEach(generationTask -> {
            if (!taskManager.isRunning(generationTask.getWorld())) {
                taskManager.start(generationTask.getWorld(), generationTask);
                sender.sendMessage(chunky.message("format_continue", chunky.message("prefix"), generationTask.getWorld().getName()));
            } else {
                sender.sendMessage(chunky.message("format_started_already", chunky.message("prefix"), generationTask.getWorld().getName()));
            }
        });
    }
}
