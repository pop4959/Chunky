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
import java.util.List;
import java.util.Optional;

import static org.popcraft.chunky.util.Translator.translate;

public class StartCommand implements ChunkyCommand {
    private final Chunky chunky;

    public StartCommand(final Chunky chunky) {
        this.chunky = chunky;
    }

    @Override
    public void execute(final Sender sender, final CommandArguments arguments) {
        if (arguments.size() > 0) {
            final Optional<World> world = arguments.next().flatMap(arg -> Input.tryWorld(chunky, arg));
            if (world.isPresent()) {
                chunky.getSelection().world(world.get());
            } else {
                sender.sendMessage(TranslationKey.HELP_START);
                return;
            }
        }
        if (arguments.size() > 1) {
            final Optional<String> shape = arguments.next().flatMap(Input::tryShape);
            if (shape.isPresent()) {
                chunky.getSelection().shape(shape.get());
            } else {
                sender.sendMessage(TranslationKey.HELP_START);
                return;
            }
        }
        if (arguments.size() > 2) {
            final Optional<Double> centerX = arguments.next().flatMap(Input::tryDoubleSuffixed).filter(c -> !Input.isPastWorldLimit(c));
            final Optional<Double> centerZ = arguments.next().flatMap(Input::tryDoubleSuffixed).filter(c -> !Input.isPastWorldLimit(c));
            if (centerX.isPresent() && centerZ.isPresent()) {
                chunky.getSelection().center(centerX.get(), centerZ.get());
            } else {
                sender.sendMessage(TranslationKey.HELP_START);
                return;
            }
        }
        if (arguments.size() > 4) {
            final Optional<Double> radiusX = arguments.next().flatMap(Input::tryDoubleSuffixed).filter(r -> r >= 0 && !Input.isPastWorldLimit(r));
            if (radiusX.isPresent()) {
                chunky.getSelection().radius(radiusX.get());
            } else {
                sender.sendMessage(TranslationKey.HELP_START);
                return;
            }
        }
        if (arguments.size() > 5) {
            final Optional<Double> radiusZ = arguments.next().flatMap(Input::tryDoubleSuffixed).filter(r -> r >= 0 && !Input.isPastWorldLimit(r));
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
    public List<String> tabSuggestions(final CommandArguments arguments) {
        if (arguments.size() == 1) {
            final List<String> suggestions = new ArrayList<>();
            chunky.getServer().getWorlds().forEach(world -> suggestions.add(world.getName()));
            return suggestions;
        } else if (arguments.size() == 2) {
            return ShapeType.ALL;
        }
        return List.of();
    }
}
