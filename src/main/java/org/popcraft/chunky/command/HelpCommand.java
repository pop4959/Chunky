package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.util.Input;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand extends ChunkyCommand {
    private List<String> helpMessages;

    public HelpCommand(Chunky chunky) {
        super(chunky);
        this.helpMessages = new ArrayList<>();
        helpMessages.add(chunky.message("help_start"));
        helpMessages.add(chunky.message("help_pause"));
        helpMessages.add(chunky.message("help_continue"));
        helpMessages.add(chunky.message("help_cancel"));
        helpMessages.add(chunky.message("help_world"));
        helpMessages.add(chunky.message("help_worldborder"));
        helpMessages.add(chunky.message("help_center"));
        helpMessages.add(chunky.message("help_spawn"));
        helpMessages.add(chunky.message("help_radius"));
        helpMessages.add(chunky.message("help_shape"));
        helpMessages.add(chunky.message("help_pattern"));
        helpMessages.add(chunky.message("help_silent"));
        helpMessages.add(chunky.message("help_quiet"));
        helpMessages.add(chunky.message("help_reload"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        final StringBuilder help = new StringBuilder();
        if (sender instanceof Player) {
            int pageIndexLast = helpMessages.size() / 8;
            int pageIndex = (args.length < 2 ? 0 : Math.max(0, Input.tryInteger(args[1]).orElse(1) - 1)) % (pageIndexLast + 1);
            int helpIndexFirst = 8 * pageIndex;
            int helpIndexLast = Math.min(helpIndexFirst + 8, helpMessages.size());
            for (int i = helpIndexFirst; i < helpIndexLast; ++i) {
                help.append('\n').append(helpMessages.get(i));
            }
            if (pageIndex != pageIndexLast) {
                help.append('\n').append(chunky.message("help_more", "/chunky help", pageIndex + 2));
            }
        } else {
            helpMessages.forEach(message -> help.append('\n').append(message));
        }
        sender.sendMessage(chunky.message("help_menu", help.toString()));
    }
}
