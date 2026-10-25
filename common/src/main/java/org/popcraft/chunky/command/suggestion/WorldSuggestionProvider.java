package org.popcraft.chunky.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.World;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class WorldSuggestionProvider<S> implements SuggestionProvider<S> {
    private final Supplier<Chunky> chunky;

    public WorldSuggestionProvider(final Supplier<Chunky> chunky) {
        this.chunky = chunky;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        final Chunky chunky = this.chunky.get();
        if (chunky == null) {
            return builder.buildFuture();
        }
        final String input = builder.getRemaining().toLowerCase();
        final List<World> worlds = chunky.getServer().getWorlds();
        List<String> suggestions = worlds.stream()
                .map(World::getKey)
                .filter(key -> key.contains(input))
                .toList();
        if (suggestions.isEmpty()) {
            suggestions = worlds.stream()
                    .filter(world -> !world.getName().equals(world.getKey()))
                    .map(World::getName)
                    .filter(name -> name.toLowerCase().contains(input))
                    .toList();
        }
        suggestions.forEach(builder::suggest);
        return builder.buildFuture();
    }
}
