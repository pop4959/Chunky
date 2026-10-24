package org.popcraft.chunky.util;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.Selection;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class TaskMetrics {
    private TaskMetrics() {
    }

    public static int activeTasks(final Chunky chunky) {
        return chunky.getGenerationTasks().size();
    }

    public static int chunksPerSecond(final Chunky chunky) {
        return (int) Math.round(chunky.getGenerationTasks().values().stream()
                .mapToDouble(task -> task.getProgress().getRate())
                .sum());
    }

    public static Map<String, Integer> shapes(final Chunky chunky) {
        return count(chunky, Selection::shape);
    }

    public static Map<String, Integer> patterns(final Chunky chunky) {
        return count(chunky, selection -> selection.pattern().getType());
    }

    public static Map<String, Integer> sizes(final Chunky chunky) {
        return count(chunky, selection -> String.valueOf((int) Math.max(selection.radiusX(), selection.radiusZ())));
    }

    public static Map<String, Integer> speeds(final Chunky chunky) {
        final Map<String, Integer> counts = new HashMap<>();
        for (final GenerationTask task : chunky.getGenerationTasks().values()) {
            counts.merge(String.valueOf((int) (task.getProgress().getRate() / 50) * 50), 1, Integer::sum);
        }
        return counts;
    }

    public static Map<String, Integer> count(final Chunky chunky, final Function<Selection, String> key) {
        final Map<String, Integer> counts = new HashMap<>();
        for (final GenerationTask task : chunky.getGenerationTasks().values()) {
            counts.merge(key.apply(task.getSelection()), 1, Integer::sum);
        }
        return counts;
    }
}
