package org.popcraft.chunky.util;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.iterator.PatternType;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.shape.ShapeType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class TaskLoader {
    private final Chunky chunky;
    private final Path savePath;
    private final Map<String, Properties> tasks = new ConcurrentHashMap<>();

    public TaskLoader(final Chunky chunky) {
        this.chunky = chunky;
        this.savePath = chunky.getConfig().getDirectory().resolve("tasks");
        reload();
    }

    public void reload() {
        try {
            Files.createDirectories(savePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (final Stream<Path> taskWalker = Files.walk(savePath)) {
            taskWalker.forEach(task -> {
                if (task.getFileName().toString().endsWith(".properties")) {
                    try (final InputStream input = Files.newInputStream(task)) {
                        final Properties properties = new Properties();
                        properties.load(input);
                        final String world = properties.getProperty("world");
                        if (world != null) {
                            tasks.put(world, properties);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Optional<GenerationTask> loadTask(final World world) {
        final Properties task = tasks.get(world.getName());
        if (task == null) {
            return Optional.empty();
        }
        final boolean cancelled = Input.tryBoolean(task.getProperty(TaskProperty.CANCELLED.key())).orElse(false);
        final double centerX = Input.tryDouble(task.getProperty(TaskProperty.CENTER_X.key())).orElse(Selection.DEFAULT_CENTER_X);
        final double centerZ = Input.tryDouble(task.getProperty(TaskProperty.CENTER_Z.key())).orElse(Selection.DEFAULT_CENTER_Z);
        final double radiusX = Input.tryDouble(task.getProperty(TaskProperty.RADIUS_X.key())).orElse(Selection.DEFAULT_RADIUS);
        final double radiusZ = Input.tryDouble(task.getProperty(TaskProperty.RADIUS_Z.key())).orElse(radiusX);
        final String pattern = task.getProperty(TaskProperty.PATTERN.key(), PatternType.REGION);
        final String file = task.getProperty(TaskProperty.CSV.key());
        final String shape = task.getProperty(TaskProperty.SHAPE.key(), ShapeType.SQUARE);
        final Selection.Builder selection = Selection.builder(chunky, world)
                .centerX(centerX)
                .centerZ(centerZ)
                .radiusX(radiusX)
                .radiusZ(radiusZ)
                .pattern(Parameter.of(pattern, file))
                .shape(shape);
        final long chunks = Input.tryLong(task.getProperty(TaskProperty.CHUNKS.key())).orElse(0L);
        final long time = Input.tryLong(task.getProperty(TaskProperty.TIME.key())).orElse(0L);
        return Optional.of(new GenerationTask(chunky, selection.build(), chunks, time, cancelled));
    }

    public List<GenerationTask> loadTasks() {
        final List<GenerationTask> generationTasks = new ArrayList<>();
        for (final World world : chunky.getServer().getWorlds()) {
            loadTask(world).ifPresent(generationTasks::add);
        }
        return generationTasks;
    }

    public void saveTask(final GenerationTask task) {
        final Selection selection = task.getSelection();
        final String world = selection.world().getName();
        final Properties properties = tasks.getOrDefault(world, new Properties());
        properties.setProperty(TaskProperty.WORLD.key(), world);
        properties.setProperty(TaskProperty.CANCELLED.key(), String.valueOf(task.isCancelled()));
        properties.setProperty(TaskProperty.CENTER_X.key(), String.valueOf(selection.centerX()));
        properties.setProperty(TaskProperty.CENTER_Z.key(), String.valueOf(selection.centerZ()));
        properties.setProperty(TaskProperty.RADIUS_X.key(), String.valueOf(selection.radiusX()));
        final String shape = task.getShape().name();
        if (ShapeType.RECTANGLE.equals(shape) || ShapeType.ELLIPSE.equals(shape)) {
            properties.setProperty(TaskProperty.RADIUS_Z.key(), String.valueOf(selection.radiusZ()));
        }
        final String pattern = task.getChunkIterator().name();
        properties.setProperty(TaskProperty.PATTERN.key(), pattern);
        if (PatternType.CSV.equals(pattern)) {
            task.getSelection().pattern().getValue().ifPresent(file -> properties.setProperty(TaskProperty.CSV.key(), file));
        }
        properties.setProperty(TaskProperty.SHAPE.key(), shape);
        properties.setProperty(TaskProperty.CHUNKS.key(), String.valueOf(task.getCount()));
        properties.setProperty(TaskProperty.TIME.key(), String.valueOf(task.getTotalTime()));
        tasks.put(world, properties);
        writeTask(world, properties);
    }

    public void saveTasks() {
        for (final GenerationTask task : chunky.getGenerationTasks().values()) {
            saveTask(task);
        }
    }

    public void cancelTask(final World world) {
        loadTask(world).ifPresent(generationTask -> {
            generationTask.stop(true);
            saveTask(generationTask);
        });
    }

    public void cancelTasks() {
        loadTasks().forEach(generationTask -> cancelTask(generationTask.getSelection().world()));
    }

    private void writeTask(final String world, final Properties properties) {
        final StringBuilder propertiesBuilder = new StringBuilder();
        for (final TaskProperty taskProperty : TaskProperty.values()) {
            final String propertyValue = properties.getProperty(taskProperty.key());
            if (propertyValue != null) {
                propertiesBuilder.append(taskProperty.key()).append('=').append(propertyValue).append('\n');
            }
        }
        final String taskFileName = world.replace(':', '/') + ".properties";
        final Path taskPath = savePath.resolve(taskFileName);
        try {
            Files.createDirectories(taskPath.getParent());
            Files.write(taskPath, propertiesBuilder.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private enum TaskProperty {
        WORLD("world"),
        CANCELLED("cancelled"),
        CENTER_X("center-x"),
        CENTER_Z("center-z"),
        RADIUS_X("radius"),
        RADIUS_Z("radius-z"),
        SHAPE("shape"),
        PATTERN("pattern"),
        CSV("csv"),
        CHUNKS("chunks"),
        TIME("time");

        private final String key;

        TaskProperty(final String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }
}
