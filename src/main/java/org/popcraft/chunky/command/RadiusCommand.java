package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.Selection;

import java.util.Optional;

public class RadiusCommand extends ChunkyCommand {
    public RadiusCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(chunky.message("help_radius"));
            return;
        }
        Optional<Integer> newRadiusX = Input.tryInteger(args[1]);
        if (!newRadiusX.isPresent() || newRadiusX.get() < 0 || newRadiusX.get() > 3e7) {
            sender.sendMessage(chunky.message("help_radius"));
            return;
        }
        Selection selection = chunky.getSelection();
        if (args.length > 2) {
            Optional<Integer> newRadiusZ = Input.tryInteger(args[2]);
            if (!newRadiusZ.isPresent() || newRadiusZ.get() < 0 || newRadiusZ.get() > 3e7) {
                sender.sendMessage(chunky.message("help_radius"));
                return;
            }
            selection.zRadius = newRadiusZ.get();
        }
        selection.radius = newRadiusX.get();
        if (args.length == 2) {
            selection.zRadius = selection.radius;
            sender.sendMessage(chunky.message("format_radius", selection.radius));
        } else {
            sender.sendMessage(chunky.message("format_radii", selection.radius, selection.zRadius));
        }
    }
}
