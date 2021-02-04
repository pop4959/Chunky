package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.popcraft.chunky.Chunky.translate;

public class StartCommand extends ChunkyCommand {
    public StartCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        final Selection selection = chunky.getSelection();
        if (args.length > 1) {
            Input.tryWorld(chunky, args[1]).ifPresent(world -> selection.world = world);
        }
        if (args.length > 2) {
            Input.tryShape(args[2]).ifPresent(shape -> selection.shape = shape);
        }
        if (args.length > 3) {
            Input.tryDoubleSuffixed(args[3]).filter(centerX -> !Input.isPastWorldLimit(centerX)).ifPresent(centerX -> selection.centerX = centerX.intValue());
        }
        if (args.length > 4) {
            Input.tryDoubleSuffixed(args[4]).filter(centerZ -> !Input.isPastWorldLimit(centerZ)).ifPresent(centerZ -> selection.centerZ = centerZ.intValue());
        }
        if (args.length > 5) {
            Input.tryIntegerSuffixed(args[5]).filter(radiusX -> radiusX >= 0 && !Input.isPastWorldLimit(radiusX)).ifPresent(radiusX -> selection.radiusX = selection.radiusZ = radiusX);
        }
        if (args.length > 6) {
            Input.tryIntegerSuffixed(args[6]).filter(radiusZ -> radiusZ >= 0 && !Input.isPastWorldLimit(radiusZ)).ifPresent(radiusZ -> selection.radiusZ = radiusZ);
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
