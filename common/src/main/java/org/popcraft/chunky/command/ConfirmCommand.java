package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.TranslationKey;

import java.util.Optional;

public class ConfirmCommand extends ChunkyCommand {
    public ConfirmCommand(final Chunky chunky) {
        super(chunky);
    }

    @Override
    public void execute(final Sender sender, final String[] args) {
        final Optional<Runnable> pendingAction = chunky.getPendingAction(sender);
        if (pendingAction.isEmpty()) {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_CONFIRM);
            return;
        }
        pendingAction.get().run();
    }
}
