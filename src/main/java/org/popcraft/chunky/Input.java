package org.popcraft.chunky;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Optional;

public class Input {
    public static Optional<World> tryWorld(String input) {
        if (input == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(Bukkit.getWorld(input));
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
