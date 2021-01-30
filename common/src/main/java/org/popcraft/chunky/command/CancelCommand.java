package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;

import static org.popcraft.chunky.Chunky.translate;

public class CancelCommand extends ChunkyCommand {
    public CancelCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        if (chunky.getGenerationTasks().size() == 0 && chunky.getConfig().loadTasks().size() == 0) {
            sender.sendMessage("format_cancel_no_tasks", translate("prefix"));
            return;
        }
        final Runnable cancelAction = () -> {
            sender.sendMessage("format_cancel", translate("prefix"));
            chunky.getConfig().cancelTasks();
            chunky.getGenerationTasks().values().forEach(generationTask -> generationTask.stop(true));
            chunky.getGenerationTasks().clear();
            chunky.getPlatform().getServer().getScheduler().cancelAsyncTasks();
        };
        chunky.setPendingAction(cancelAction);
        sender.sendMessage("format_cancel_confirm", translate("prefix"));
    }
}
