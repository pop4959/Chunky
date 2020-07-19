package org.popcraft.chunky;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public final class Utils {

    public static List<String> getWorldNamesFilteredByName(String worldName) {
        final String worldNameLowerCase = worldName.toLowerCase();

        return Bukkit.getWorlds()
                .stream()
                .map(World::getName)
                .map(String::toLowerCase)
                .filter(w -> w.startsWith(worldNameLowerCase))
                .collect(toList());
    }

    public static Optional<Integer> tryParseInteger(String input) {
        try {
            return Optional.of(Integer.parseInt(input));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

}
