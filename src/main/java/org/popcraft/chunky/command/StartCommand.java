package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.Selection;

public class StartCommand extends ChunkyCommand {
    public StartCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(CommandSender sender, String[] args) {
        Selection selection = chunky.getSelection();
        if (chunky.getGenerationTasks().containsKey(selection.world)) {
            sender.sendMessage(chunky.message("format_started_already", selection.world.getName()));
            return;
        }
        GenerationTask generationTask = new GenerationTask(chunky, selection);
        chunky.getGenerationTasks().put(selection.world, generationTask);
        chunky.getServer().getScheduler().runTaskAsynchronously(chunky, generationTask);
        sender.sendMessage(chunky.message("format_start", selection.world.getName(), selection.centerX, selection.centerZ, selection.radiusX));
    }
}
