package org.popcraft.chunky.command;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

public final class CommandArguments {
    private final int size;
    private final Queue<String> args = new LinkedList<>();

    private CommandArguments(final List<String> arguments) {
        this.size = arguments.size();
        this.args.addAll(arguments);
    }

    public static CommandArguments of(final List<String> arguments) {
        return new CommandArguments(arguments);
    }

    public static CommandArguments of(final String... arguments) {
        return new CommandArguments(List.of(arguments));
    }

    public static CommandArguments empty() {
        return new CommandArguments(List.of());
    }

    public int size() {
        return size;
    }

    public Optional<String> next() {
        return Optional.ofNullable(args.poll());
    }

    public List<String> remaining() {
        final List<String> arguments = new ArrayList<>(args);
        args.clear();
        return arguments;
    }

    public String joined() {
        return String.join(" ", remaining());
    }
}
