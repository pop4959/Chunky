package org.popcraft.chunky.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.popcraft.chunky.command.CommandLiteral;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TrimModeSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    private static final List<String> TRIM_MODES = List.of("inside", "outside");

    @Override
    public CompletableFuture<Suggestions> getSuggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        try {
            final String input = context.getArgument(CommandLiteral.TRIM_MODE, String.class);
            TRIM_MODES.forEach(shape -> {
                if (shape.contains(input.toLowerCase())) {
                    builder.suggest(shape);
                }
            });
        } catch (IllegalArgumentException e) {
            TRIM_MODES.forEach(builder::suggest);
        }
        return builder.buildFuture();
    }
}
