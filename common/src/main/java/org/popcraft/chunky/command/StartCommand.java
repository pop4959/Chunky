package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.Sender;

import static org.popcraft.chunky.Chunky.translate;

public class StartCommand extends ChunkyCommand {
    public StartCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        final Selection selection = chunky.getSelection();
        if (chunky.getGenerationTasks().containsKey(selection.world)) {
            sender.sendMessage("format_started_already", translate("prefix"), selection.world.getName());
            return;
        }
        final Runnable startAction = () -> {
            GenerationTask generationTask = new GenerationTask(chunky, selection);
            chunky.getGenerationTasks().put(selection.world, generationTask);
            chunky.getPlatform().getServer().getScheduler().runTaskAsync(generationTask);
            String radius = selection.radiusX == selection.radiusZ ? String.valueOf(selection.radiusX) : String.format("%d, %d", selection.radiusX, selection.radiusZ);
            sender.sendMessage("format_start", translate("prefix"), selection.world.getName(), selection.centerX, selection.centerZ, radius);
        };
        if (chunky.getConfig().loadTask(selection.world).isPresent()) {
            chunky.setPendingAction(startAction);
            sender.sendMessage("format_start_confirm", translate("prefix"));
        } else {
            startAction.run();
        }
    }
}
