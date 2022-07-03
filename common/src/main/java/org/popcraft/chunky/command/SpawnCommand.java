package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Formatting;
import org.popcraft.chunky.util.TranslationKey;

public class SpawnCommand extends ChunkyCommand {
    public SpawnCommand(final Chunky chunky) {
        super(chunky);
    }

    @Override
    public void execute(final Sender sender, final String[] args) {
        chunky.getSelection().spawn();
        final Selection current = chunky.getSelection().build();
        sender.sendMessagePrefixed(TranslationKey.FORMAT_CENTER, Formatting.number(current.centerX()), Formatting.number(current.centerZ()));
    }
}
