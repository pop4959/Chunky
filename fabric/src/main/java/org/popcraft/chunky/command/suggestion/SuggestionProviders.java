package org.popcraft.chunky.command.suggestion;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.command.ServerCommandSource;

public class SuggestionProviders {
    public static final SuggestionProvider<ServerCommandSource> PATTERNS;
    public static final SuggestionProvider<ServerCommandSource> SHAPES;

    static {
        PATTERNS = new PatternSuggestionProvider();
        SHAPES = new ShapeSuggestionProvider();
    }

    private SuggestionProviders() {
    }
}
