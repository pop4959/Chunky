package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.TranslationKey;

import java.util.List;

import static org.popcraft.chunky.util.Translator.translate;

public class HelpCommand implements ChunkyCommand {
    private final Chunky chunky;
    private final List<String> helpMessages = List.of(
            TranslationKey.HELP_START,
            TranslationKey.HELP_PAUSE,
            TranslationKey.HELP_CONTINUE,
            TranslationKey.HELP_CANCEL,
            TranslationKey.HELP_WORLD,
            TranslationKey.HELP_WORLDBORDER,
            TranslationKey.HELP_CENTER,
            TranslationKey.HELP_SPAWN,
            TranslationKey.HELP_RADIUS,
            TranslationKey.HELP_CORNERS,
            TranslationKey.HELP_SHAPE,
            TranslationKey.HELP_PATTERN,
            TranslationKey.HELP_SILENT,
            TranslationKey.HELP_QUIET,
            TranslationKey.HELP_TRIM,
            TranslationKey.HELP_PROGRESS,
            TranslationKey.HELP_RELOAD
    );

    public HelpCommand(final Chunky chunky) {
        this.chunky = chunky;
    }

    @Override
    public void execute(final Sender sender, final CommandArguments arguments) {
        final StringBuilder help = new StringBuilder();
        if (sender.isPlayer()) {
            final int pageIndexLast = helpMessages.size() / 8;
            final int pageIndex = (arguments.size() < 1 ? 0 : Math.max(0, arguments.next().flatMap(Input::tryInteger).orElse(1) - 1)) % (pageIndexLast + 1);
            final int helpIndexFirst = 8 * pageIndex;
            final int helpIndexLast = Math.min(helpIndexFirst + 8, helpMessages.size());
            for (int i = helpIndexFirst; i < helpIndexLast; ++i) {
                help.append('\n').append(translate(helpMessages.get(i)));
            }
            if (pageIndex != pageIndexLast) {
                help.append('\n').append(translate(TranslationKey.HELP_MORE, "/chunky help " + (pageIndex + 2)));
            }
        } else {
            helpMessages.forEach(message -> help.append('\n').append(translate(message)));
        }
        sender.sendMessage(TranslationKey.HELP_MENU, help.toString());
    }

    @Override
    public List<String> suggestions(final CommandArguments arguments) {
        return List.of();
    }
}
