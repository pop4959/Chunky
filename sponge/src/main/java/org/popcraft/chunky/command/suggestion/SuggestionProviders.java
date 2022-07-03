package org.popcraft.chunky.command.suggestion;

public final class SuggestionProviders {
    public static final PatternSuggestionProvider PATTERNS;
    public static final ShapeSuggestionProvider SHAPES;

    static {
        PATTERNS = new PatternSuggestionProvider();
        SHAPES = new ShapeSuggestionProvider();
    }

    private SuggestionProviders() {
    }
}
