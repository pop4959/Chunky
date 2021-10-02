package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.TranslationKey;

import java.util.ArrayList;
import java.util.List;

import static org.popcraft.chunky.util.Translator.translate;

public class HelpCommand extends ChunkyCommand {
    private final List<String> helpMessages;

    public HelpCommand(Chunky chunky) {
        super(chunky);
        this.helpMessages = new ArrayList<>();
        helpMessages.add(TranslationKey.HELP_START);
        helpMessages.add(TranslationKey.HELP_PAUSE);
        helpMessages.add(TranslationKey.HELP_CONTINUE);
        helpMessages.add(TranslationKey.HELP_CANCEL);
        helpMessages.add(TranslationKey.HELP_WORLD);
        helpMessages.add(TranslationKey.HELP_WORLDBORDER);
        helpMessages.add(TranslationKey.HELP_CENTER);
        helpMessages.add(TranslationKey.HELP_SPAWN);
        helpMessages.add(TranslationKey.HELP_RADIUS);
        helpMessages.add(TranslationKey.HELP_CORNERS);
        helpMessages.add(TranslationKey.HELP_SHAPE);
        helpMessages.add(TranslationKey.HELP_PATTERN);
        helpMessages.add(TranslationKey.HELP_SILENT);
        helpMessages.add(TranslationKey.HELP_QUIET);
        helpMessages.add(TranslationKey.HELP_RELOAD);
    }

    @Override
    public void execute(Sender sender, String[] args) {
        final StringBuilder help = new StringBuilder();
        if (sender.isPlayer()) {
            int pageIndexLast = helpMessages.size() / 8;
            int pageIndex = (args.length < 2 ? 0 : Math.max(0, Input.tryInteger(args[1]).orElse(1) - 1)) % (pageIndexLast + 1);
            int helpIndexFirst = 8 * pageIndex;
            int helpIndexLast = Math.min(helpIndexFirst + 8, helpMessages.size());
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
}
