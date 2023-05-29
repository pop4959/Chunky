package org.popcraft.chunky.command.suggestion;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.command.ServerCommandSource;

public final class SuggestionProviders {
    public static final SuggestionProvider<ServerCommandSource> PATTERNS;
    public static final SuggestionProvider<ServerCommandSource> SHAPES;
    public static final SuggestionProvider<ServerCommandSource> TRIM_MODES;

    static {
        PATTERNS = new PatternSuggestionProvider();
        SHAPES = new ShapeSuggestionProvider();
        TRIM_MODES = new TrimModeSuggestionProvider();
    }

    private SuggestionProviders() {
    }
}
