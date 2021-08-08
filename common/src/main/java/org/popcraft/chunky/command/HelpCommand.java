package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Input;

import java.util.ArrayList;
import java.util.List;

import static org.popcraft.chunky.util.Translator.translate;

public class HelpCommand extends ChunkyCommand {
    private final List<String> helpMessages;

    public HelpCommand(Chunky chunky) {
        super(chunky);
        this.helpMessages = new ArrayList<>();
        helpMessages.add("help_start");
        helpMessages.add("help_pause");
        helpMessages.add("help_continue");
        helpMessages.add("help_cancel");
        helpMessages.add("help_world");
        helpMessages.add("help_worldborder");
        helpMessages.add("help_center");
        helpMessages.add("help_spawn");
        helpMessages.add("help_radius");
        helpMessages.add("help_corners");
        helpMessages.add("help_shape");
        helpMessages.add("help_pattern");
        helpMessages.add("help_silent");
        helpMessages.add("help_quiet");
        helpMessages.add("help_reload");
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
                help.append('\n').append(translate("help_more", "/chunky help " + (pageIndex + 2)));
            }
        } else {
            helpMessages.forEach(message -> help.append('\n').append(translate(message)));
        }
        sender.sendMessage("help_menu", help.toString());
    }
}
