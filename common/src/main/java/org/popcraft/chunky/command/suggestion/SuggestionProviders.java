package org.popcraft.chunky.command.suggestion;

import com.mojang.brigadier.suggestion.SuggestionProvider;

public final class SuggestionProviders<S> {
    public final SuggestionProvider<S> PATTERNS;
    public final SuggestionProvider<S> SHAPES;
    public final SuggestionProvider<S> TRIM_MODES;

    public SuggestionProviders() {
        PATTERNS = new PatternSuggestionProvider<>();
        SHAPES = new ShapeSuggestionProvider<>();
        TRIM_MODES = new TrimModeSuggestionProvider<>();
    }
}
