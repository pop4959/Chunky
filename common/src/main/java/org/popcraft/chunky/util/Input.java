package org.popcraft.chunky.util;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.iterator.PatternType;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.shape.ShapeType;

import java.util.Optional;

public final class Input {
    private Input() {
    }

    public static Optional<World> tryWorld(final Chunky chunky, final String input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }
        return chunky.getServer().getWorld(input);
    }

    public static Optional<String> tryPattern(final String input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }
        final String inputLower = input.toLowerCase();
        if (PatternType.ALL.contains(inputLower)) {
            return Optional.of(inputLower);
        }
        return Optional.empty();
    }

    public static Optional<String> tryShape(final String input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }
        final String inputLower = input.toLowerCase();
        if (ShapeType.ALL.contains(inputLower)) {
            return Optional.of(inputLower);
        }
        return Optional.empty();
    }

    public static Optional<Integer> tryInteger(final String input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(input));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Optional<Integer> tryIntegerSuffixed(final String input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }
        final int last = input.length() - 1;
        return suffixValue(input.charAt(last))
                .map(suffixValue -> tryInteger(input.substring(0, last)).map(i -> i * suffixValue))
                .orElse(tryInteger(input));
    }

    public static Optional<Double> tryDouble(final String input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Double.parseDouble(input));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Optional<Double> tryDoubleSuffixed(final String input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }
        final int last = input.length() - 1;
        return suffixValue(input.charAt(last))
                .map(suffixValue -> tryDouble(input.substring(0, last)).map(d -> d * suffixValue))
                .orElse(tryDouble(input));
    }

    public static Optional<Integer> trySign(final String input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }
        final char sign = input.charAt(0);
        return switch (sign) {
            case '-' -> Optional.of(-1);
            case '+' -> Optional.of(1);
            default -> Optional.empty();
        };
    }

    public static boolean isPastWorldLimit(final double value) {
        return Math.abs(value) > 3e7;
    }

    public static String checkLanguage(final String language) {
        return Translator.isValidLanguage(language) ? language : "en";
    }

    private static Optional<Integer> suffixValue(final char suffix) {
        return switch (Character.toLowerCase(suffix)) {
            case 'c' -> Optional.of(16);
            case 'r' -> Optional.of(512);
            case 'k' -> Optional.of(1000);
            default -> Optional.empty();
        };
    }
}
