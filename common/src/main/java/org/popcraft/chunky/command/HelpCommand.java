package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Input;

import java.util.ArrayList;
import java.util.List;

import static org.popcraft.chunky.util.Translator.translate;

public class HelpCommand extends ChunkyCommand {
    private List<String> helpMessages;

    public HelpCommand(Chunky chunky) {
        super(chunky);
        this.helpMessages = new ArrayList<>();
        helpMessages.add(translate("help_start"));
        helpMessages.add(translate("help_pause"));
        helpMessages.add(translate("help_continue"));
        helpMessages.add(translate("help_cancel"));
        helpMessages.add(translate("help_world"));
        helpMessages.add(translate("help_worldborder"));
        helpMessages.add(translate("help_center"));
        helpMessages.add(translate("help_spawn"));
        helpMessages.add(translate("help_radius"));
        helpMessages.add(translate("help_corners"));
        helpMessages.add(translate("help_shape"));
        helpMessages.add(translate("help_pattern"));
        helpMessages.add(translate("help_silent"));
        helpMessages.add(translate("help_quiet"));
        helpMessages.add(translate("help_reload"));
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
                help.append('\n').append(helpMessages.get(i));
            }
            if (pageIndex != pageIndexLast) {
                help.append('\n').append(translate("help_more", "/chunky help " + (pageIndex + 2)));
            }
        } else {
            helpMessages.forEach(message -> help.append('\n').append(message));
        }
        sender.sendMessage("help_menu", help.toString());
    }
}
