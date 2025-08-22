package org.popcraft.chunky.platform;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.command.ChunkyCommand;
import org.popcraft.chunky.command.CommandArguments;
import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.command.brigadier.BrigadierChunkyCommand;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public class PaperChunkyCommand extends BrigadierChunkyCommand<CommandSourceStack, World, PlayerSelectorArgumentResolver> {

    private final Chunky chunky;
    private final Function<CommandSender, Sender> commandSenderFunction;
    private final Function<Player, Sender> playerSenderFunction;

    public PaperChunkyCommand(Chunky chunky, Function<CommandSender, Sender> commandSenderFunction, Function<Player, Sender> playerSenderFunction) {
        this.chunky = chunky;
        this.commandSenderFunction = commandSenderFunction;
        this.playerSenderFunction = playerSenderFunction;
    }

    @Override
    protected LiteralArgumentBuilder<CommandSourceStack> literal(String literal) {
        return Commands.literal(literal);
    }

    @Override
    protected <T> RequiredArgumentBuilder<CommandSourceStack, T> argument(String name, ArgumentType<T> argumentType) {
        return Commands.argument(name, argumentType);
    }

    @Override
    protected ArgumentType<World> dimensionArgumentType() {
        return ArgumentTypes.world();
    }

    @Override
    protected ArgumentType<PlayerSelectorArgumentResolver> playerArgumentType() {
        return ArgumentTypes.player();
    }

    @Override
    protected boolean rootRequirement(CommandSourceStack source) {
        return source.getSender().hasPermission("chunky.command");
    }

    @Override
    protected boolean borderRequirement(CommandSourceStack source) {
        return chunky != null && chunky.getCommands().containsKey(CommandLiteral.BORDER);
    }

    @Override
    protected int rootExecutes(CommandContext<CommandSourceStack> context) {
        final Sender sender;
        if (context.getSource().getExecutor() instanceof final Player player) {
            sender = playerSenderFunction.apply(player);
        } else {
            sender = commandSenderFunction.apply(context.getSource().getSender());
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
