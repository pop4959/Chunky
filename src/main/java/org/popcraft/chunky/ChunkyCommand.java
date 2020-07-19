package org.popcraft.chunky;

import org.bukkit.command.CommandSender;

import java.util.function.BiConsumer;

public interface ChunkyCommand extends BiConsumer<CommandSender, CommandArguments> {
}
