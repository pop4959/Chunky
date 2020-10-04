package org.popcraft.chunky.command;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;

import java.util.Map;

public class ContinueCommand extends ChunkyCommand {
    public ContinueCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(CommandSender sender, String[] args) {
        if (chunky.getConfigStorage().loadTasks().size() == 0) {
            sender.sendMessage(chunky.message("format_continue_no_tasks", chunky.message("prefix")));
            return;
        }
        final Map<World, GenerationTask> generationTasks = chunky.getGenerationTasks();
        chunky.getConfigStorage().loadTasks().forEach(generationTask -> {
            if (!generationTasks.containsKey(generationTask.getWorld())) {
                generationTasks.put(generationTask.getWorld(), generationTask);
                chunky.getServer().getScheduler().runTaskAsynchronously(chunky, generationTask);
                sender.sendMessage(chunky.message("format_continue", chunky.message("prefix"), generationTask.getWorld().getName()));
            } else {
                sender.sendMessage(chunky.message("format_started_already", chunky.message("prefix"), generationTask.getWorld().getName()));
            }
        });
    }
}
