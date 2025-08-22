package org.popcraft.chunky.command.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.command.suggestion.SuggestionProviders;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public abstract class BrigadierChunkyCommand<S, D, P> {

    protected abstract LiteralArgumentBuilder<S> literal(String literal);

    protected abstract <T> RequiredArgumentBuilder<S, T> argument(String name, ArgumentType<T> argumentType);

    protected abstract ArgumentType<D> dimensionArgumentType();

    protected abstract ArgumentType<P> playerArgumentType();


    protected abstract boolean rootRequirement(S source);

    protected abstract boolean borderRequirement(S source);

    protected abstract int rootExecutes(CommandContext<S> context);


    public LiteralArgumentBuilder<S> construct(SuggestionProviders<S> suggestionProviders) {
        final LiteralArgumentBuilder<S> command = literal(CommandLiteral.CHUNKY)
            .requires(this::rootRequirement)
            .executes(this::rootExecutes);
        registerArguments(command, literal(CommandLiteral.CANCEL),
            argument(CommandLiteral.WORLD, dimensionArgumentType()));
        registerArguments(command, literal(CommandLiteral.CENTER),
            argument(CommandLiteral.X, word()),
            argument(CommandLiteral.Z, word()));
        registerArguments(command, literal(CommandLiteral.CONFIRM));
        registerArguments(command, literal(CommandLiteral.CONTINUE),
            argument(CommandLiteral.WORLD, dimensionArgumentType()));
        registerArguments(command, literal(CommandLiteral.CORNERS),
            argument(CommandLiteral.X1, word()),
            argument(CommandLiteral.Z1, word()),
            argument(CommandLiteral.X2, word()),
            argument(CommandLiteral.Z2, word()));
        registerArguments(command, literal(CommandLiteral.HELP),
            argument(CommandLiteral.PAGE, integer()));
        registerArguments(command, literal(CommandLiteral.PATTERN),
            argument(CommandLiteral.PATTERN, string()).suggests(suggestionProviders.PATTERNS),
            argument(CommandLiteral.VALUE, string()));
        registerArguments(command, literal(CommandLiteral.PAUSE),
            argument(CommandLiteral.WORLD, dimensionArgumentType()));
        registerArguments(command, literal(CommandLiteral.PROGRESS));
        registerArguments(command, literal(CommandLiteral.QUIET),
            argument(CommandLiteral.INTERVAL, integer()));
        registerArguments(command, literal(CommandLiteral.RADIUS),
            argument(CommandLiteral.RADIUS, word()),
            argument(CommandLiteral.RADIUS, word()));
        registerArguments(command, literal(CommandLiteral.RELOAD),
            argument(CommandLiteral.TYPE, word()));
        registerArguments(command, literal(CommandLiteral.SELECTION));
        registerArguments(command, literal(CommandLiteral.SHAPE),
            argument(CommandLiteral.SHAPE, string()).suggests(suggestionProviders.SHAPES));
        registerArguments(command, literal(CommandLiteral.SILENT));
        registerArguments(command, literal(CommandLiteral.SPAWN));
        registerArguments(command, literal(CommandLiteral.START),
            argument(CommandLiteral.WORLD, dimensionArgumentType()),
            argument(CommandLiteral.SHAPE, string()).suggests(suggestionProviders.SHAPES),
            argument(CommandLiteral.CENTER_X, word()),
            argument(CommandLiteral.CENTER_Z, word()),
            argument(CommandLiteral.RADIUS_X, word()),
            argument(CommandLiteral.RADIUS_Z, word()));
        registerArguments(command, literal(CommandLiteral.TRIM),
            argument(CommandLiteral.WORLD, dimensionArgumentType()),
            argument(CommandLiteral.SHAPE, string()).suggests(suggestionProviders.SHAPES),
            argument(CommandLiteral.CENTER_X, word()),
            argument(CommandLiteral.CENTER_Z, word()),
            argument(CommandLiteral.RADIUS_X, word()),
            argument(CommandLiteral.RADIUS_Z, word()),
            argument(CommandLiteral.TRIM_MODE, string()).suggests(suggestionProviders.TRIM_MODES),
            argument(CommandLiteral.INHABITED, word()));
        registerArguments(command, literal(CommandLiteral.WORLDBORDER));
        registerArguments(command, literal(CommandLiteral.WORLD),
            argument(CommandLiteral.WORLD, dimensionArgumentType()));

        final LiteralArgumentBuilder<S> borderCommand = literal(CommandLiteral.BORDER)
            .requires(this::borderRequirement)
            .executes(command.getCommand());
        registerArguments(borderCommand, literal(CommandLiteral.ADD),
            argument(CommandLiteral.WORLD, dimensionArgumentType()),
            argument(CommandLiteral.SHAPE, string()).suggests(suggestionProviders.SHAPES),
            argument(CommandLiteral.CENTER_X, word()),
            argument(CommandLiteral.CENTER_Z, word()),
            argument(CommandLiteral.RADIUS_X, word()),
            argument(CommandLiteral.RADIUS_Z, word()));
        registerArguments(borderCommand, literal(CommandLiteral.BYPASS),
            argument(CommandLiteral.PLAYER, playerArgumentType()));
        registerArguments(borderCommand, literal(CommandLiteral.HELP));
        registerArguments(borderCommand, literal(CommandLiteral.LIST));
        registerArguments(borderCommand, literal(CommandLiteral.LOAD),
            argument(CommandLiteral.WORLD, dimensionArgumentType()));
        registerArguments(borderCommand, literal(CommandLiteral.REMOVE),
            argument(CommandLiteral.WORLD, dimensionArgumentType()));
        registerArguments(borderCommand, literal(CommandLiteral.WRAP),
            argument(CommandLiteral.WRAP, word()));
        registerArguments(command, borderCommand);

        return command;
    }

    @SafeVarargs
    private void registerArguments(final LiteralArgumentBuilder<S> command, final ArgumentBuilder<S, ?>... arguments) {
        for (int i = arguments.length - 1; i > 0; --i) {
            arguments[i - 1].then(arguments[i].executes(command.getCommand()));
        }
        command.then(arguments[0].executes(command.getCommand()));
    }
}
