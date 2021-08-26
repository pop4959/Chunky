package org.popcraft.chunky.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import org.popcraft.chunky.iterator.PatternType;

import java.util.concurrent.CompletableFuture;

public class PatternSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        try {
            final String input = context.getArgument("pattern", String.class);
            PatternType.ALL.forEach(pattern -> {
                if (pattern.contains(input.toLowerCase())) {
                    builder.suggest(pattern);
                }
            });
        } catch (IllegalArgumentException e) {
            PatternType.ALL.forEach(builder::suggest);
        }
        return builder.buildFuture();
    }
}
