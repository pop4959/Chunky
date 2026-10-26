package org.popcraft.chunky.command.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.ChunkyForge;
import org.popcraft.chunky.platform.ForgePlayer;
import org.popcraft.chunky.platform.ForgeSender;
import org.popcraft.chunky.platform.Sender;

public class ForgeCommand extends BrigadierCommand<CommandSourceStack> {
    private final ChunkyForge plugin;

    public ForgeCommand(final ChunkyForge plugin) {
        this.plugin = plugin;
    }

    @Override
    protected Chunky getChunky() {
        return plugin.getChunky();
    }

    @Override
    protected Sender getSender(final CommandSourceStack source) {
        if (source.getEntity() instanceof final ServerPlayer player) {
            return new ForgePlayer(player);
        }
        return new ForgeSender(source);
    }

    @Override
    protected ArgumentType<?> worldArgumentType() {
        return DimensionArgument.dimension();
    }

    @Override
    protected boolean hasPermission(final CommandSourceStack source) {
        final MinecraftServer server = source.getServer();
        //noinspection ConstantValue
        if (server != null && server.isSingleplayer()) {
            return true;
        }
        return source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }
}
