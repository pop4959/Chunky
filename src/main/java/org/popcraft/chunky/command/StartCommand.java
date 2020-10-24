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
        final Selection selection = chunky.getSelection();
        if (chunky.getGenerationTasks().containsKey(selection.world)) {
            sender.sendMessage(chunky.message("format_started_already", chunky.message("prefix"), selection.world.getName()));
            return;
        }
        final Runnable startAction = () -> {
            GenerationTask generationTask = new GenerationTask(chunky, selection);
            chunky.getGenerationTasks().put(selection.world, generationTask);
            chunky.getServer().getScheduler().runTaskAsynchronously(chunky, generationTask);
            String radius = selection.radiusX == selection.radiusZ ? String.valueOf(selection.radiusX) : String.format("%d, %d", selection.radiusX, selection.radiusZ);
            sender.sendMessage(chunky.message("format_start", chunky.message("prefix"), selection.world.getName(), selection.centerX, selection.centerZ, radius));
        };
        if (chunky.getConfigStorage().loadTask(selection.world).isPresent()) {
            chunky.setPendingAction(startAction);
            sender.sendMessage(chunky.message("format_start_confirm", chunky.message("prefix")));
        } else {
            startAction.run();
        }
    }
}
