package org.popcraft.chunky.command.suggestion;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;

public class SuggestionProviders {
    public static final SuggestionProvider<CommandSource> PATTERNS;
    public static final SuggestionProvider<CommandSource> SHAPES;

    static {
        PATTERNS = new PatternSuggestionProvider();
        SHAPES = new ShapeSuggestionProvider();
    }
}
