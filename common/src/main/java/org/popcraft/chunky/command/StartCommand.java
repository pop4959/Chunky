package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.util.Input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.popcraft.chunky.Chunky.translate;

public class StartCommand extends ChunkyCommand {
    public StartCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        final Selection selection = chunky.getSelection();
        if (args.length > 1) {
            Optional<World> world = Input.tryWorld(chunky, args[1]);
            if (world.isPresent()) {
                selection.world = world.get();
            } else {
                sender.sendMessage("help_start");
                return;
            }
        }
        if (args.length > 2) {
            Optional<String> shape = Input.tryShape(args[2]);
            if (shape.isPresent()) {
                selection.shape = shape.get();
            } else {
                sender.sendMessage("help_start");
                return;
            }
        }
        if (args.length > 3) {
            Optional<Double> centerX = Input.tryDoubleSuffixed(args[3]).filter(cx -> !Input.isPastWorldLimit(cx));
            Optional<Double> centerZ = Input.tryDoubleSuffixed(args.length > 4 ? args[4] : null).filter(cz -> !Input.isPastWorldLimit(cz));
            if (centerX.isPresent() && centerZ.isPresent()) {
                selection.centerX = centerX.get().intValue();
                selection.centerZ = centerZ.get().intValue();
            } else {
                sender.sendMessage("help_start");
                return;
            }
        }
        if (args.length > 5) {
            Optional<Integer> radiusX = Input.tryIntegerSuffixed(args[5]).filter(rx -> rx >= 0 && !Input.isPastWorldLimit(rx));
            if (radiusX.isPresent()) {
                selection.radiusX = selection.radiusZ = radiusX.get();
            } else {
                sender.sendMessage("help_start");
                return;
            }
        }
        if (args.length > 6) {
            Optional<Integer> radiusZ = Input.tryIntegerSuffixed(args[6]).filter(rz -> rz >= 0 && !Input.isPastWorldLimit(rz));
            if (radiusZ.isPresent()) {
                selection.radiusZ = radiusZ.get();
            } else {
                sender.sendMessage("help_start");
                return;
            }
        }
        if (chunky.getGenerationTasks().containsKey(selection.world)) {
            sender.sendMessage("format_started_already", translate("prefix"), selection.world.getName());
            return;
        }
        final Runnable startAction = () -> {
            GenerationTask generationTask = new GenerationTask(chunky, selection);
            chunky.getGenerationTasks().put(selection.world, generationTask);
            chunky.getPlatform().getServer().getScheduler().runTaskAsync(generationTask);
            String radius = selection.radiusX == selection.radiusZ ? String.valueOf(selection.radiusX) : String.format("%d, %d", selection.radiusX, selection.radiusZ);
            sender.sendMessage("format_start", translate("prefix"), selection.world.getName(), selection.centerX, selection.centerZ, radius);
        };
        if (chunky.getConfig().loadTask(selection.world).isPresent()) {
            chunky.setPendingAction(startAction);
            sender.sendMessage("format_start_confirm", translate("prefix"));
        } else {
            startAction.run();
        }
    }

    @Override
    public List<String> tabSuggestions(Sender sender, String[] args) {
        if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();
            chunky.getPlatform().getServer().getWorlds().forEach(world -> suggestions.add(world.getName()));
            return suggestions;
        } else if (args.length == 3) {
            return Input.SHAPES;
        }
        return Collections.emptyList();
    }
}
