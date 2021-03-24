package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;

import java.util.Optional;

import static org.popcraft.chunky.Chunky.translate;

public class ConfirmCommand extends ChunkyCommand {
    public ConfirmCommand(Chunky chunky) {
        super(chunky);
    }

    @Override
    public void execute(Sender sender, String[] args) {
        Optional<Runnable> pendingAction = chunky.getPendingAction(sender);
        if (!pendingAction.isPresent()) {
            sender.sendMessage("format_confirm", translate("prefix"));
            return;
        }
        pendingAction.get().run();
    }
}
