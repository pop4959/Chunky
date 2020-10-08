package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.task.GenerationTask;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.task.TaskManager;

public class StartCommand extends ChunkyCommand {
    public StartCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(CommandSender sender, String[] args) {
        Selection selection = chunky.getSelection();
        TaskManager taskManager = chunky.getTaskManager();
        if (taskManager.isRunning(selection.world)) {
            sender.sendMessage(chunky.message("format_started_already", chunky.message("prefix"), selection.world.getName()));
            return;
        }
        GenerationTask generationTask = new GenerationTask(chunky, selection);
        taskManager.start(generationTask.getWorld(), generationTask);
        String radius = selection.radiusX == selection.radiusZ ? String.valueOf(selection.radiusX) : String.format("%d, %d", selection.radiusX, selection.radiusZ);
        sender.sendMessage(chunky.message("format_start", chunky.message("prefix"), selection.world.getName(), selection.centerX, selection.centerZ, radius));
    }
}
