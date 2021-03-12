package org.popcraft.chunky.util;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.World;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Input {
    public static final List<String> SHAPES = Arrays.asList("circle", "diamond", "ellipse", "pentagon", "rectangle", "square", "star", "triangle");
    public static final List<String> LANGUAGES = Arrays.asList("bg", "de", "es", "fr", "nl", "pl", "pt", "ru", "tr", "zh_CN");

    public static Optional<World> tryWorld(Chunky chunky, String input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }
        return chunky.getPlatform().getServer().getWorld(input);
    }

    public static Optional<String> tryShape(String input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }
        String inputLower = input.toLowerCase();
        if (SHAPES.contains(inputLower)) {
            return Optional.of(inputLower);
        }
        return Optional.empty();
    }

    public static Optional<Integer> tryInteger(String input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(input));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Optional<Integer> tryIntegerSuffixed(String input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }
        final int last = input.length() - 1;
        return suffixValue(input.charAt(last))
                .map(suffixValue -> tryInteger(input.substring(0, last)).map(i -> i * suffixValue))
                .orElse(tryInteger(input));
    }

    public static Optional<Double> tryDouble(String input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Double.parseDouble(input));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Optional<Double> tryDoubleSuffixed(String input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }
        final int last = input.length() - 1;
        return suffixValue(input.charAt(last))
                .map(suffixValue -> tryDouble(input.substring(0, last)).map(d -> d * suffixValue))
                .orElse(tryDouble(input));
    }

    public static boolean isPastWorldLimit(double value) {
        return Math.abs(value) > 3e7;
    }

    public static String checkLanguage(String language) {
        return LANGUAGES.contains(language) ? language : "en";
    }

    private static Optional<Integer> suffixValue(char suffix) {
        switch (Character.toLowerCase(suffix)) {
            case 'c':
                return Optional.of(16);
            case 'r':
                return Optional.of(512);
            case 'k':
                return Optional.of(1000);
            default:
                return Optional.empty();
        }
    }
}
