package org.popcraft.chunky.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.shape.ShapeType;

import java.util.concurrent.CompletableFuture;

public class ShapeSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        try {
            final String input = context.getArgument(CommandLiteral.SHAPE, String.class);
            ShapeType.ALL.forEach(shape -> {
                if (shape.contains(input.toLowerCase())) {
                    builder.suggest(shape);
                }
            });
        } catch (IllegalArgumentException e) {
            ShapeType.ALL.forEach(builder::suggest);
        }
        return builder.buildFuture();
    }
}
