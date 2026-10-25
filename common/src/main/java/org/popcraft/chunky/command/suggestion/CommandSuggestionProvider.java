package org.popcraft.chunky.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.command.ChunkyCommand;
import org.popcraft.chunky.command.CommandArguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class CommandSuggestionProvider<S> implements SuggestionProvider<S> {
    private final Supplier<Chunky> chunky;

    public CommandSuggestionProvider(final Supplier<Chunky> chunky) {
        this.chunky = chunky;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        final Chunky chunky = this.chunky.get();
        final List<ParsedCommandNode<S>> nodes = context.getLastChild().getNodes();
        if (chunky == null || nodes.isEmpty()) {
            return builder.buildFuture();
        }
        final String input = builder.getInput().substring(nodes.getFirst().getRange().getStart(), builder.getStart());
        final String[] tokens = input.split(" ");
        final ChunkyCommand command = tokens.length > 1 ? chunky.getCommands().get(tokens[1]) : null;
        if (command == null) {
            return builder.buildFuture();
        }
        final List<String> arguments = new ArrayList<>(Arrays.asList(tokens).subList(2, tokens.length));
        arguments.add(builder.getRemaining());
        final String remaining = builder.getRemaining().toLowerCase();
        command.suggestions(CommandArguments.of(arguments)).stream()
                .filter(suggestion -> suggestion.toLowerCase().contains(remaining))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
