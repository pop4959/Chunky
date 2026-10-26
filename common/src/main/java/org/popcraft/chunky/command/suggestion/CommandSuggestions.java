package org.popcraft.chunky.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.command.ChunkyCommand;
import org.popcraft.chunky.command.CommandArguments;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class CommandSuggestions {
    private CommandSuggestions() {
    }

    public static <S> CompletableFuture<Suggestions> suggest(final Chunky chunky, final CommandContext<S> context, final SuggestionsBuilder builder) {
        final List<ParsedCommandNode<S>> nodes = context.getLastChild().getNodes();
        if (chunky == null || nodes.size() < 2) {
            return builder.buildFuture();
        }
        final String input = builder.getInput();
        final ChunkyCommand command = chunky.getCommands().get(nodes.get(1).getRange().get(input));
        if (command == null) {
            return builder.buildFuture();
        }
        final List<String> arguments = new ArrayList<>();
        for (final ParsedCommandNode<S> node : nodes.subList(2, nodes.size())) {
            if (node.getRange().getStart() >= builder.getStart()) {
                break;
            }
            final String argument = node.getRange().get(input);
            if (argument.isEmpty()) {
                return builder.buildFuture();
            }
            arguments.add(argument);
        }
        arguments.add(builder.getRemaining());
        final String remaining = builder.getRemaining().toLowerCase();
        command.suggestions(CommandArguments.of(arguments)).stream()
                .filter(suggestion -> suggestion.toLowerCase().contains(remaining))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
