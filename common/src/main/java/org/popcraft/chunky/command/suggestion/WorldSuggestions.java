package org.popcraft.chunky.command.suggestion;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.World;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class WorldSuggestions {
    private WorldSuggestions() {
    }

    public static CompletableFuture<Suggestions> suggest(final Chunky chunky, final SuggestionsBuilder builder) {
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
