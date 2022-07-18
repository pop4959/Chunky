package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.TranslationKey;

import java.util.List;

import static org.popcraft.chunky.util.Translator.translate;

public class SilentCommand implements ChunkyCommand {
    private final Chunky chunky;

    public SilentCommand(final Chunky chunky) {
        this.chunky = chunky;
    }

    @Override
    public void execute(final Sender sender, final CommandArguments arguments) {
        chunky.getConfig().setSilent(!chunky.getConfig().isSilent());
        final String status = translate(chunky.getConfig().isSilent() ? TranslationKey.ENABLED : TranslationKey.DISABLED);
        sender.sendMessagePrefixed(TranslationKey.FORMAT_SILENT, status);
    }

    @Override
    public List<String> tabSuggestions(final CommandArguments arguments) {
        return List.of();
    }
}
