package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;

import static org.popcraft.chunky.Chunky.translate;

public class ConfirmCommand extends ChunkyCommand {
    public ConfirmCommand(Chunky chunky) {
        super(chunky);
    }

    @Override
    public void execute(Sender sender, String[] args) {
        Runnable pendingAction = chunky.getPendingAction();
        if (pendingAction == null) {
            sender.sendMessage("format_confirm", translate("prefix"));
            return;
        }
        chunky.setPendingAction(null);
        pendingAction.run();
    }
}
