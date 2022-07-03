package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.shape.ShapeType;
import org.popcraft.chunky.util.Formatting;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.TranslationKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.popcraft.chunky.util.Translator.translate;

public class StartCommand extends ChunkyCommand {
    public StartCommand(final Chunky chunky) {
        super(chunky);
    }

    public void execute(final Sender sender, final String[] args) {
        if (args.length > 1) {
            final Optional<World> world = Input.tryWorld(chunky, args[1]);
            if (world.isPresent()) {
                chunky.getSelection().world(world.get());
            } else {
                sender.sendMessage(TranslationKey.HELP_START);
                return;
            }
        }
        if (args.length > 2) {
            final Optional<String> shape = Input.tryShape(args[2]);
            if (shape.isPresent()) {
                chunky.getSelection().shape(shape.get());
            } else {
                sender.sendMessage(TranslationKey.HELP_START);
                return;
            }
        }
        if (args.length > 3) {
            final Optional<Double> centerX = Input.tryDoubleSuffixed(args[3]).filter(cx -> !Input.isPastWorldLimit(cx));
            final Optional<Double> centerZ = Input.tryDoubleSuffixed(args.length > 4 ? args[4] : null).filter(cz -> !Input.isPastWorldLimit(cz));
            if (centerX.isPresent() && centerZ.isPresent()) {
                chunky.getSelection().center(centerX.get(), centerZ.get());
            } else {
                sender.sendMessage(TranslationKey.HELP_START);
                return;
            }
        }
        if (args.length > 5) {
            final Optional<Double> radiusX = Input.tryDoubleSuffixed(args[5]).filter(rx -> rx >= 0 && !Input.isPastWorldLimit(rx));
            if (radiusX.isPresent()) {
                chunky.getSelection().radius(radiusX.get());
            } else {
                sender.sendMessage(TranslationKey.HELP_START);
                return;
            }
        }
        if (args.length > 6) {
            final Optional<Double> radiusZ = Input.tryDoubleSuffixed(args[6]).filter(rz -> rz >= 0 && !Input.isPastWorldLimit(rz));
            if (radiusZ.isPresent()) {
                chunky.getSelection().radiusZ(radiusZ.get());
            } else {
                sender.sendMessage(TranslationKey.HELP_START);
                return;
            }
        }
        final Selection current = chunky.getSelection().build();
        if (chunky.getGenerationTasks().containsKey(current.world().getName())) {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_STARTED_ALREADY, current.world().getName());
            return;
        }
        if (current.radiusX() > chunky.getLimit()) {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_START_LIMIT, Formatting.number(chunky.getLimit()));
            return;
        }
        final Runnable startAction = () -> {
            final GenerationTask generationTask = new GenerationTask(chunky, current);
            chunky.getGenerationTasks().put(current.world().getName(), generationTask);
            chunky.getScheduler().runTask(generationTask);
            sender.sendMessagePrefixed(TranslationKey.FORMAT_START, current.world().getName(), translate("shape_" + current.shape()), Formatting.number(current.centerX()), Formatting.number(current.centerZ()), Formatting.radius(current));
        };
        if (chunky.getConfig().loadTask(current.world()).filter(task -> !task.isCancelled()).isPresent()) {
            chunky.setPendingAction(sender, startAction);
            sender.sendMessagePrefixed(TranslationKey.FORMAT_START_CONFIRM, "/chunky continue", "/chunky confirm");
        } else {
            startAction.run();
        }
    }

    @Override
    public List<String> tabSuggestions(final String[] args) {
        if (args.length == 2) {
            final List<String> suggestions = new ArrayList<>();
            chunky.getServer().getWorlds().forEach(world -> suggestions.add(world.getName()));
            return suggestions;
        } else if (args.length == 3) {
            return ShapeType.ALL;
        }
        return Collections.emptyList();
    }
}
