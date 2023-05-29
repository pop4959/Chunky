package org.popcraft.chunky.command.suggestion;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;

public final class SuggestionProviders {
    public static final SuggestionProvider<CommandSourceStack> PATTERNS;
    public static final SuggestionProvider<CommandSourceStack> SHAPES;
    public static final SuggestionProvider<CommandSourceStack> TRIM_MODES;

    static {
        PATTERNS = new PatternSuggestionProvider();
        SHAPES = new ShapeSuggestionProvider();
        TRIM_MODES = new TrimModeSuggestionProvider();
    }

    private SuggestionProviders() {
    }
}
