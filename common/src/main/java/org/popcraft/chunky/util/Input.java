package org.popcraft.chunky.util;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.World;

import java.util.Optional;

public class Input {
    public static Optional<World> tryWorld(Chunky chunky, String input) {
        if (input == null) {
            return Optional.empty();
        }
        return chunky.getPlatform().getServer().getWorld(input);
    }

    public static Optional<Integer> tryInteger(String input) {
        if (input == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(input));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Optional<Double> tryDouble(String input) {
        if (input == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Double.parseDouble(input));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
