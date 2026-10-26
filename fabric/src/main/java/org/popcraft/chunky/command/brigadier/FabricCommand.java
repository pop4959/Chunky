package org.popcraft.chunky.command.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.ChunkyFabric;
import org.popcraft.chunky.platform.FabricPlayer;
import org.popcraft.chunky.platform.FabricSender;
import org.popcraft.chunky.platform.Sender;

public class FabricCommand extends BrigadierCommand<CommandSourceStack> {
    private final ChunkyFabric plugin;

    public FabricCommand(final ChunkyFabric plugin) {
        this.plugin = plugin;
    }

    @Override
    protected Chunky getChunky() {
        return plugin.getChunky();
    }

    @Override
    protected Sender getSender(final CommandSourceStack source) {
        if (source.getEntity() instanceof final ServerPlayer player) {
            return new FabricPlayer(player);
        }
        return new FabricSender(source);
    }

    @Override
    protected ArgumentType<?> worldArgumentType() {
        return DimensionArgument.dimension();
    }

    @Override
    protected boolean hasPermission(final CommandSourceStack source) {
        final MinecraftServer server = source.getServer();
        if (server != null && server.isSingleplayer()) {
            return true;
        }
        return new FabricSender(source).hasPermission("chunky", "command", true);
    }
}
