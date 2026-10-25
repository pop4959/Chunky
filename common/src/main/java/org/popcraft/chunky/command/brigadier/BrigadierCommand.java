package org.popcraft.chunky.command.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.command.ChunkyCommand;
import org.popcraft.chunky.command.CommandArguments;
import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.command.suggestion.CommandSuggestionProvider;
import org.popcraft.chunky.command.suggestion.WorldSuggestionProvider;
import org.popcraft.chunky.platform.Sender;

import java.util.Arrays;
import java.util.Map;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public abstract class BrigadierCommand<S> {
    private final SuggestionProvider<S> suggestions = new CommandSuggestionProvider<>(this::getChunky);
    private final SuggestionProvider<S> worldSuggestions = new WorldSuggestionProvider<>(this::getChunky);

    protected abstract Chunky getChunky();

    protected abstract Sender getSender(S source);

    protected abstract ArgumentType<?> worldArgumentType();

    protected ArgumentType<?> greedyWorldArgumentType() {
        return worldArgumentType();
    }

    protected abstract boolean hasPermission(S source);

    protected boolean hasPermission(final S source, final String command) {
        return true;
    }

    public LiteralArgumentBuilder<S> build() {
        final LiteralArgumentBuilder<S> command = literal(CommandLiteral.CHUNKY)
                .requires(this::hasPermission)
                .executes(this::execute);
        registerCommand(command, CommandLiteral.CANCEL,
                greedyWorld());
        registerCommand(command, CommandLiteral.CENTER,
                argument(CommandLiteral.X, word()),
                argument(CommandLiteral.Z, word()));
        registerCommand(command, CommandLiteral.CONFIRM);
        registerCommand(command, CommandLiteral.CONTINUE,
                greedyWorld());
        registerCommand(command, CommandLiteral.CORNERS,
                argument(CommandLiteral.X1, word()),
                argument(CommandLiteral.Z1, word()),
                argument(CommandLiteral.X2, word()),
                argument(CommandLiteral.Z2, word()));
        registerCommand(command, CommandLiteral.HELP,
                argument(CommandLiteral.PAGE, integer()));
        registerCommand(command, CommandLiteral.PATTERN,
                argument(CommandLiteral.PATTERN, string()),
                argument(CommandLiteral.VALUE, string()));
        registerCommand(command, CommandLiteral.PAUSE,
                greedyWorld());
        registerCommand(command, CommandLiteral.PROGRESS);
        registerCommand(command, CommandLiteral.QUIET,
                argument(CommandLiteral.INTERVAL, integer()));
        registerCommand(command, CommandLiteral.RADIUS,
                argument(CommandLiteral.RADIUS, word()),
                argument(CommandLiteral.RADIUS, word()));
        registerCommand(command, CommandLiteral.RELOAD,
                argument(CommandLiteral.TYPE, word()));
        registerCommand(command, CommandLiteral.SELECTION);
        registerCommand(command, CommandLiteral.SHAPE,
                argument(CommandLiteral.SHAPE, string()));
        registerCommand(command, CommandLiteral.SILENT);
        registerCommand(command, CommandLiteral.SPAWN);
        registerCommand(command, CommandLiteral.START,
                world(),
                argument(CommandLiteral.SHAPE, string()),
                argument(CommandLiteral.CENTER_X, word()),
                argument(CommandLiteral.CENTER_Z, word()),
                argument(CommandLiteral.RADIUS_X, word()),
                argument(CommandLiteral.RADIUS_Z, word()));
        registerCommand(command, CommandLiteral.TRIM,
                world(),
                argument(CommandLiteral.SHAPE, string()),
                argument(CommandLiteral.CENTER_X, word()),
                argument(CommandLiteral.CENTER_Z, word()),
                argument(CommandLiteral.RADIUS_X, word()),
                argument(CommandLiteral.RADIUS_Z, word()),
                argument(CommandLiteral.TRIM_MODE, string()),
                argument(CommandLiteral.INHABITED, word()));
        registerCommand(command, CommandLiteral.WORLDBORDER,
                world());
        registerCommand(command, CommandLiteral.WORLD,
                greedyWorld());
        final LiteralArgumentBuilder<S> borderCommand = literal(CommandLiteral.BORDER)
                .requires(source -> isBorderAvailable() && hasPermission(source, CommandLiteral.BORDER));
        registerArguments(borderCommand, literal(CommandLiteral.ADD),
                world(),
                argument(CommandLiteral.SHAPE, string()),
                argument(CommandLiteral.CENTER_X, word()),
                argument(CommandLiteral.CENTER_Z, word()),
                argument(CommandLiteral.RADIUS_X, word()),
                argument(CommandLiteral.RADIUS_Z, word()));
        registerArguments(borderCommand, literal(CommandLiteral.BYPASS),
                argument(CommandLiteral.PLAYER, word()));
        registerArguments(borderCommand, literal(CommandLiteral.HELP));
        registerArguments(borderCommand, literal(CommandLiteral.LIST));
        registerArguments(borderCommand, literal(CommandLiteral.LOAD),
                world());
        registerArguments(borderCommand, literal(CommandLiteral.REMOVE),
                world());
        registerArguments(borderCommand, literal(CommandLiteral.WRAP),
                argument(CommandLiteral.WRAP, word()));
        registerArguments(command, borderCommand);
        return command;
    }

    private int execute(final CommandContext<S> context) {
        final Map<String, ChunkyCommand> commands = getChunky().getCommands();
        final String input = context.getInput().substring(context.getLastChild().getNodes().getFirst().getRange().getStart());
        final String[] tokens = input.split(" ");
        final String subCommand = tokens.length > 1 && commands.containsKey(tokens[1]) ? tokens[1] : CommandLiteral.HELP;
        final CommandArguments arguments = tokens.length > 2 ? CommandArguments.of(Arrays.copyOfRange(tokens, 2, tokens.length)) : CommandArguments.empty();
        commands.get(subCommand).execute(getSender(context.getSource()), arguments);
        return Command.SINGLE_SUCCESS;
    }

    private boolean isBorderAvailable() {
        final Chunky chunky = getChunky();
        return chunky != null && chunky.getCommands().containsKey(CommandLiteral.BORDER);
    }

    private LiteralArgumentBuilder<S> literal(final String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    private <T> RequiredArgumentBuilder<S, T> argument(final String name, final ArgumentType<T> type) {
        return argument(name, type, suggestions);
    }

    private <T> RequiredArgumentBuilder<S, T> argument(final String name, final ArgumentType<T> type, final SuggestionProvider<S> provider) {
        return RequiredArgumentBuilder.<S, T>argument(name, type).suggests(provider);
    }

    private RequiredArgumentBuilder<S, ?> world() {
        return argument(CommandLiteral.WORLD, worldArgumentType(), worldSuggestions);
    }

    private RequiredArgumentBuilder<S, ?> greedyWorld() {
        return argument(CommandLiteral.WORLD, greedyWorldArgumentType(), worldSuggestions);
    }

    @SafeVarargs
    private void registerCommand(final LiteralArgumentBuilder<S> command, final String name, final ArgumentBuilder<S, ?>... arguments) {
        registerArguments(command, literal(name).requires(source -> hasPermission(source, name)), arguments);
    }

    @SafeVarargs
    private void registerArguments(final LiteralArgumentBuilder<S> command, final LiteralArgumentBuilder<S> literal, final ArgumentBuilder<S, ?>... arguments) {
        for (int i = arguments.length - 1; i > 0; --i) {
            arguments[i - 1].then(arguments[i].executes(this::execute));
        }
        if (arguments.length > 0) {
            literal.then(arguments[0].executes(this::execute));
        }
        command.then(literal.executes(this::execute));
    }
}
