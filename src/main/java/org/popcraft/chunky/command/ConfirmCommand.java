package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;

public class ConfirmCommand extends ChunkyCommand {
    public ConfirmCommand(Chunky chunky) {
        super(chunky);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Runnable pendingAction = chunky.getPendingAction();
        if (pendingAction == null) {
            sender.sendMessage(chunky.message("format_confirm", chunky.message("prefix")));
            return;
        }
        chunky.setPendingAction(null);
        pendingAction.run();
    }
}
