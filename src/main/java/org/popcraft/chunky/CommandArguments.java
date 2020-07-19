package org.popcraft.chunky;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CommandArguments {

    private final List<String> arguments;

    public CommandArguments(String[] arguments) {
        this.arguments = Arrays.asList(arguments);
    }

    public int size() {
        return arguments.size();
    }

    public Optional<String> getString(int index) {
        return Optional.of(index)
                .filter(this::inRange)
                .map(arguments::get);
    }

    public Optional<Integer> getInt(int index) {
        return getString(index)
                .flatMap(Utils::tryParseInteger);
    }

    private boolean inRange(int index) {
        return arguments.size() < index;
    }

}
