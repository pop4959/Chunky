package org.popcraft.chunky.util;

import java.util.Optional;

public class Parameter {
    private final String type;
    private final String value;

    public Parameter(final String type, final String value) {
        this.type = type;
        this.value = value;
    }

    public Parameter(final String expression) {
        final String[] parts = expression.split("=");
        this.type = parts[0];
        this.value = parts.length > 1 ? parts[1] : null;
    }

    public static Parameter of(final String expression) {
        return new Parameter(expression);
    }

    public static Parameter of(final String type, final String value) {
        return new Parameter(type, value);
    }

    public String getType() {
        return type;
    }

    public Optional<String> getValue() {
        return Optional.ofNullable(value);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(type);
        if (value != null) {
            builder.append("=").append(value);
        }
        return builder.toString();
    }
}
