package org.popcraft.chunky.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.shape.ShapeType;

import java.util.concurrent.CompletableFuture;

public class ShapeSuggestionProvider <S> implements SuggestionProvider<S> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        try {
            final String input = context.getArgument(CommandLiteral.SHAPE, String.class);
            ShapeType.all().forEach(shape -> {
                if (shape.contains(input.toLowerCase())) {
                    builder.suggest(shape);
                }
            });
        } catch (IllegalArgumentException e) {
            ShapeType.all().forEach(builder::suggest);
        }
        return builder.buildFuture();
    }
}
