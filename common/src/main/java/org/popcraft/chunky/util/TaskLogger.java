package org.popcraft.chunky.util;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.api.event.task.GenerationCompleteEvent;
import org.popcraft.chunky.api.event.task.GenerationStartEvent;
import org.popcraft.chunky.event.EventBus;
import org.popcraft.chunky.shape.ShapeType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TaskLogger {
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Chunky chunky;

    public TaskLogger(final Chunky chunky) {
        this.chunky = chunky;
    }

    public void register(final EventBus eventBus) {
        eventBus.subscribe(GenerationStartEvent.class, this::onStart);
        eventBus.subscribe(GenerationCompleteEvent.class, this::onComplete);
    }

    private void onStart(final GenerationStartEvent event) {
        final StringBuilder line = new StringBuilder(event.continued() ? "continued" : "started");
        appendTask(line, event.world(), event.owner(), event.centerX(), event.centerZ(), event.radiusX(), event.radiusZ(), event.shape(), event.pattern());
        write(line);
    }

    private void onComplete(final GenerationCompleteEvent event) {
        final StringBuilder line = new StringBuilder(event.reason().name().toLowerCase(Locale.ROOT));
        appendTask(line, event.world(), event.owner(), event.centerX(), event.centerZ(), event.radiusX(), event.radiusZ(), event.shape(), event.pattern());
        line.append(" chunks=").append(event.chunks());
        final long seconds = event.time() / 1000;
        line.append(" time=").append(String.format("%d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60));
        write(line);
    }

    private void appendTask(final StringBuilder line, final String world, final String owner, final double centerX, final double centerZ, final double radiusX, final double radiusZ, final String shape, final String pattern) {
        line.append(" world=").append(world);
        if (owner != null) {
            line.append(" owner=").append(owner);
        }
        line.append(" center-x=").append(number(centerX));
        line.append(" center-z=").append(number(centerZ));
        line.append(" radius=").append(number(radiusX));
        if (ShapeType.RECTANGLE.equals(shape) || ShapeType.ELLIPSE.equals(shape)) {
            line.append(" radius-z=").append(number(radiusZ));
        }
        line.append(" shape=").append(shape);
        final Parameter parameter = Parameter.of(pattern);
        line.append(" pattern=").append(parameter.getType());
        parameter.getValue().ifPresent(file -> line.append(" csv=").append(file));
    }

    private String number(final double value) {
        if (value == Math.rint(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private synchronized void write(final StringBuilder line) {
        final String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        final Path logPath = chunky.getConfig().getDataDirectory().resolve("tasks.log");
        try {
            Files.createDirectories(logPath.getParent());
            Files.writeString(logPath, '[' + timestamp + "] " + line + '\n', StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
