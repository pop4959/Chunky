package org.popcraft.chunky.commands.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.command.ChunkyCommand;
import org.popcraft.chunky.command.CommandArguments;
import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.command.brigadier.BrigadierChunkyCommand;
import org.popcraft.chunky.platform.ForgePlayer;
import org.popcraft.chunky.platform.ForgeSender;
import org.popcraft.chunky.platform.Sender;

import java.util.Arrays;
import java.util.Map;

public class ForgeChunkyCommand extends BrigadierChunkyCommand<CommandSourceStack, ResourceLocation, EntitySelector> {

    private final Chunky chunky;

    public ForgeChunkyCommand(Chunky chunky) {
        this.chunky = chunky;
    }

    protected LiteralArgumentBuilder<CommandSourceStack> literal(String literal) {
        return LiteralArgumentBuilder.literal(literal);
    }

    protected <T> RequiredArgumentBuilder<CommandSourceStack, T> argument(String name, ArgumentType<T> argumentType) {
        return RequiredArgumentBuilder.argument(name, argumentType);
    }

    protected ArgumentType<ResourceLocation> dimensionArgumentType() {
        return DimensionArgument.dimension();
    }

    protected ArgumentType<EntitySelector> playerArgumentType() {
        return EntityArgument.player();
    }

    protected boolean rootRequirement(CommandSourceStack source) {
        final MinecraftServer server = source.getServer();
        //noinspection ConstantValue
        if (server != null && server.isSingleplayer()) {
            return true;
        }
        return source.hasPermission(2);
    }

    protected boolean borderRequirement(CommandSourceStack source) {
        return chunky != null && chunky.getCommands().containsKey(CommandLiteral.BORDER);
    }

    protected int rootExecutes(CommandContext<CommandSourceStack> context) {
        final Sender sender;
        if (context.getSource().getEntity() instanceof final ServerPlayer player) {
            sender = new ForgePlayer(player);
        } else {
            sender = new ForgeSender(context.getSource());
        }
        final Map<String, ChunkyCommand> commands = chunky.getCommands();
        final String input = context.getInput().substring(context.getLastChild().getNodes().get(0).getRange().getStart());
        final String[] tokens = input.split(" ");
        final String subCommand = tokens.length > 1 && commands.containsKey(tokens[1]) ? tokens[1] : CommandLiteral.HELP;
        final CommandArguments arguments = tokens.length > 2 ? CommandArguments.of(Arrays.copyOfRange(tokens, 2, tokens.length)) : CommandArguments.empty();
        commands.get(subCommand).execute(sender, arguments);
        return Command.SINGLE_SUCCESS;
    }
}
