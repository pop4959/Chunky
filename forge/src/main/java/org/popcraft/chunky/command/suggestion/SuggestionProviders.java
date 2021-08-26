package org.popcraft.chunky.command.suggestion;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;

public class SuggestionProviders {
    public static final SuggestionProvider<CommandSourceStack> PATTERNS;
    public static final SuggestionProvider<CommandSourceStack> SHAPES;

    static {
        PATTERNS = new PatternSuggestionProvider();
        SHAPES = new ShapeSuggestionProvider();
    }

    private SuggestionProviders() {
    }
}
