package org.popcraft.chunky.trim;

import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.util.Input;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class TrimFilters {
    private static final List<String> KEYS = List.of(CommandLiteral.INHABITED, CommandLiteral.UNMODIFIED);
    private static final String RANGE = "..";
    private static final long TICKS_PER_SECOND = 20;

    private TrimFilters() {
    }

    public static List<String> keys() {
        return KEYS;
    }

    public static List<TrimFilter> parse(final List<String> tokens) {
        final Map<String, TrimFilter> filters = new LinkedHashMap<>();
        for (int i = 0; i < tokens.size(); ++i) {
            final String token = tokens.get(i);
            final int separator = token.indexOf('=');
            final String key;
            final Optional<? extends TrimFilter> filter;
            if (separator < 0) {
                key = CommandLiteral.INHABITED;
                filter = i == 0 ? Input.tryIntegerSuffixed(token).map(InhabitedTimeFilter::new) : Optional.empty();
            } else {
                key = token.substring(0, separator).toLowerCase(Locale.ROOT);
                final String value = token.substring(separator + 1);
                filter = switch (key) {
                    case CommandLiteral.INHABITED -> parseInhabited(value);
                    case CommandLiteral.UNMODIFIED -> parseUnmodified(value);
                    default -> Optional.empty();
                };
            }
            if (filter.isEmpty() || filters.putIfAbsent(key, filter.get()) != null) {
                throw new IllegalArgumentException(token);
            }
        }
        return new ArrayList<>(filters.values());
    }

    public static List<String> suggestions(final List<String> tokens) {
        final List<String> used = tokens.subList(0, tokens.size() - 1).stream()
                .map(token -> token.indexOf('=') < 0 ? CommandLiteral.INHABITED : token.substring(0, token.indexOf('=')).toLowerCase(Locale.ROOT))
                .toList();
        return KEYS.stream()
                .filter(key -> !used.contains(key))
                .map(key -> key + "=")
                .toList();
    }

    private static Optional<InhabitedTimeFilter> parseInhabited(final String value) {
        final int range = value.indexOf(RANGE);
        if (range < 0) {
            return parseTicks(value).map(InhabitedTimeFilter::new);
        }
        if (range > 0) {
            return Optional.empty();
        }
        return parseTicks(value.substring(RANGE.length())).map(InhabitedTimeFilter::new);
    }

    private static Optional<UnmodifiedFilter> parseUnmodified(final String value) {
        final long now = System.currentTimeMillis() / 1000;
        return timeUnit(value).flatMap(unit -> parseAmount(value.substring(0, value.length() - 1))
                .map(amount -> new UnmodifiedFilter(amount, unit, now - Math.round(amount * unit.toSeconds(1)))));
    }

    private static Optional<Long> parseTicks(final String value) {
        final Optional<TimeUnit> unit = timeUnit(value);
        final String amount = unit.isPresent() ? value.substring(0, value.length() - 1) : value;
        final long multiplier = unit.map(timeUnit -> timeUnit.toSeconds(1) * TICKS_PER_SECOND).orElse(1L);
        return parseAmount(amount).map(number -> Math.round(number * multiplier));
    }

    private static Optional<TimeUnit> timeUnit(final String value) {
        if (value.isEmpty()) {
            return Optional.empty();
        }
        return switch (Character.toLowerCase(value.charAt(value.length() - 1))) {
            case 's' -> Optional.of(TimeUnit.SECONDS);
            case 'm' -> Optional.of(TimeUnit.MINUTES);
            case 'h' -> Optional.of(TimeUnit.HOURS);
            case 'd' -> Optional.of(TimeUnit.DAYS);
            default -> Optional.empty();
        };
    }

    private static Optional<Double> parseAmount(final String amount) {
        if (amount.isEmpty() || !Character.isDigit(amount.charAt(amount.length() - 1))) {
            return Optional.empty();
        }
        return Input.tryDouble(amount).filter(number -> Double.isFinite(number) && number >= 0);
    }
}
