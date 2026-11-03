package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.util.Formatting;
import org.popcraft.chunky.util.GenerationMap;
import org.popcraft.chunky.util.TranslationKey;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class DebugCommand implements ChunkyCommand {
    private final Chunky chunky;

    public DebugCommand(final Chunky chunky) {
        this.chunky = chunky;
    }

    @Override
    public void execute(final Sender sender, final CommandArguments arguments) {
        final String option = arguments.next().map(value -> value.toLowerCase(Locale.ROOT)).orElse("");
        if (!CommandLiteral.MAP.equals(option)) {
            sender.sendMessage(TranslationKey.HELP_DEBUG);
            return;
        }
        final Selection selection = chunky.getSelection().build();
        final World world = selection.world();
        sender.sendMessagePrefixed(TranslationKey.FORMAT_DEBUG_MAP_START, world.getName());
        final Path path = chunky.getConfig().getDirectory().resolve("debug").resolve(world.getName().replace(':', '/') + ".png");
        chunky.getScheduler().runTask(() -> {
            final GenerationMap map = new GenerationMap(selection);
            try {
                map.scan();
                map.write(path);
            } catch (final IOException e) {
                sender.sendMessagePrefixed(TranslationKey.FORMAT_DEBUG_MAP_FAILED, world.getName(), String.valueOf(e.getMessage()));
                return;
            }
            final String missing = (map.isExact() ? "" : "~") + Formatting.number(map.getCount(GenerationMap.MISSING));
            sender.sendMessagePrefixed(TranslationKey.FORMAT_DEBUG_MAP, world.getName(), path.toString(),
                    Formatting.number(map.getCount(GenerationMap.FULL)),
                    Formatting.number(map.getCount(GenerationMap.UNFINISHED)),
                    Formatting.number(map.getCount(GenerationMap.INCOMPLETE)),
                    Formatting.number(map.getCount(GenerationMap.INVALID)),
                    missing);
        });
    }

    @Override
    public List<String> suggestions(final CommandArguments arguments) {
        if (arguments.size() == 1) {
            return List.of(CommandLiteral.MAP);
        }
        return List.of();
    }
}
