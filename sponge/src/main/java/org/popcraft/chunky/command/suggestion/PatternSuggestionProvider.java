package org.popcraft.chunky.command.suggestion;

import org.popcraft.chunky.iterator.PatternType;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;

import java.util.ArrayList;
import java.util.List;

public class PatternSuggestionProvider implements ValueCompleter {
    @Override
    public List<CommandCompletion> complete(final CommandContext context, final String currentInput) {
        final List<CommandCompletion> completions = new ArrayList<>();
        PatternType.ALL.forEach(pattern -> {
            if (pattern.contains(currentInput.toLowerCase())) {
                completions.add(CommandCompletion.of(pattern));
            }
        });
        return completions;
    }
}
