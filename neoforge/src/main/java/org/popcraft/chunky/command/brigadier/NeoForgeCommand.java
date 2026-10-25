package org.popcraft.chunky.command.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.NeoForgePlayer;
import org.popcraft.chunky.platform.NeoForgeSender;
import org.popcraft.chunky.platform.Sender;

import java.util.function.Supplier;

public class NeoForgeCommand extends BrigadierCommand<CommandSourceStack> {
    private final Supplier<Chunky> chunky;

    public NeoForgeCommand(final Supplier<Chunky> chunky) {
        this.chunky = chunky;
    }

    @Override
    protected Chunky getChunky() {
        return chunky.get();
    }

    @Override
    protected Sender getSender(final CommandSourceStack source) {
        if (source.getEntity() instanceof final ServerPlayer player) {
            return new NeoForgePlayer(player);
        }
        return new NeoForgeSender(source);
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
