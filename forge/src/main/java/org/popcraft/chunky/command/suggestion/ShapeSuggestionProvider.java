package org.popcraft.chunky.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import org.popcraft.chunky.util.Input;

import java.util.concurrent.CompletableFuture;

public class ShapeSuggestionProvider implements SuggestionProvider<CommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
        try {
            final String input = context.getArgument("shape", String.class);
            Input.SHAPES.forEach(shape -> {
                if (shape.contains(input.toLowerCase())) {
                    builder.suggest(shape);
                }
            });
        } catch (IllegalArgumentException e) {
            Input.SHAPES.forEach(builder::suggest);
        }
        return builder.buildFuture();
    }
}
