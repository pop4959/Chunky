package org.popcraft.chunky.command.suggestion;

import org.popcraft.chunky.util.Input;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;

import java.util.ArrayList;
import java.util.List;

public class ShapeSuggestionProvider implements ValueCompleter {
    @Override
    public List<CommandCompletion> complete(CommandContext context, String currentInput) {
        List<CommandCompletion> completions = new ArrayList<>();
        Input.SHAPES.forEach(pattern -> {
            if (pattern.contains(currentInput.toLowerCase())) {
                completions.add(CommandCompletion.of(pattern));
            }
        });
        return completions;
    }
}
