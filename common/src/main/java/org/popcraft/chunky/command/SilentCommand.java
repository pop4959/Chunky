package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.TranslationKey;

import static org.popcraft.chunky.util.Translator.translate;

public class SilentCommand extends ChunkyCommand {
    public SilentCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        chunky.getConfig().setSilent(!chunky.getConfig().isSilent());
        String status = translate(chunky.getConfig().isSilent() ? TranslationKey.ENABLED : TranslationKey.DISABLED);
        sender.sendMessagePrefixed(TranslationKey.FORMAT_SILENT, status);
    }
}
