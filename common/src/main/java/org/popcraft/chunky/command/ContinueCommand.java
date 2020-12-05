package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.World;

import java.util.List;
import java.util.Map;

import static org.popcraft.chunky.Chunky.translate;

public class ContinueCommand extends ChunkyCommand {
    public ContinueCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        final List<GenerationTask> loadTasks = chunky.getConfig().loadTasks();
        if (loadTasks.size() == 0) {
            sender.sendMessage("format_continue_no_tasks", translate("prefix"));
            return;
        }
        final Map<World, GenerationTask> generationTasks = chunky.getGenerationTasks();
        loadTasks.forEach(generationTask -> {
            if (!generationTasks.containsKey(generationTask.getWorld())) {
                generationTasks.put(generationTask.getWorld(), generationTask);
                chunky.getPlatform().getServer().getScheduler().runTaskAsync(generationTask);
                sender.sendMessage("format_continue", translate("prefix"), generationTask.getWorld().getName());
            } else {
                sender.sendMessage("format_started_already", translate("prefix"), generationTask.getWorld().getName());
            }
        });
    }
}
