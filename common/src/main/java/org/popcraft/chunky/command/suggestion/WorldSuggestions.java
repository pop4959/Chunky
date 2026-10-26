package org.popcraft.chunky.command.suggestion;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.World;

import java.util.concurrent.CompletableFuture;

public final class WorldSuggestions {
    private WorldSuggestions() {
    }

    public static CompletableFuture<Suggestions> suggest(final Chunky chunky, final SuggestionsBuilder builder) {
        if (chunky == null) {
            return builder.buildFuture();
        }
        final String input = builder.getRemaining().toLowerCase();
        chunky.getServer().getWorlds().stream()
                .map(World::getKey)
                .filter(key -> key.contains(input))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
