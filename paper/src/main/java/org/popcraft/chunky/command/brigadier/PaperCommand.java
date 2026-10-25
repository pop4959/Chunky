package org.popcraft.chunky.command.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.command.brigadier.argument.WorldArgumentType;
import org.popcraft.chunky.platform.PaperPlayer;
import org.popcraft.chunky.platform.PaperSender;
import org.popcraft.chunky.platform.Sender;

public class PaperCommand extends BrigadierCommand<CommandSourceStack> {
    private static final String COMMAND_PERMISSION_KEY = "chunky.command";
    private final Chunky chunky;

    public PaperCommand(final Chunky chunky) {
        this.chunky = chunky;
    }

    @Override
    protected Chunky getChunky() {
        return chunky;
    }

    @Override
    protected Sender getSender(final CommandSourceStack source) {
        if (source.getExecutor() instanceof final Player player) {
            return new PaperPlayer(player);
        }
        return new PaperSender(source.getSender());
    }

    @Override
    protected ArgumentType<?> worldArgumentType() {
        return new WorldArgumentType();
    }

    @Override
    protected ArgumentType<?> greedyWorldArgumentType() {
        return StringArgumentType.greedyString();
    }

    @Override
    protected boolean hasPermission(final CommandSourceStack source) {
        return source.getSender().hasPermission(COMMAND_PERMISSION_KEY);
    }

    @Override
    protected boolean hasPermission(final CommandSourceStack source, final String command) {
        return source.getSender().hasPermission(COMMAND_PERMISSION_KEY + "." + command);
    }
}
