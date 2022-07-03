package org.popcraft.chunky.platform.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.iterator.PatternType;
import org.popcraft.chunky.platform.Config;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.shape.ShapeType;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.Parameter;
import org.popcraft.chunky.util.Translator;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class GsonConfig implements Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Supplier<Chunky> chunky;
    private final Path savePath;
    private ConfigModel configModel = new ConfigModel();

    public GsonConfig(final Supplier<Chunky> chunky, final Path savePath) {
        this.chunky = chunky;
        this.savePath = savePath;
        if (Files.exists(this.savePath)) {
            reload();
        } else {
            saveConfig();
        }
        Translator.setLanguage(getLanguage());
    }

    @Override
    public Path getDirectory() {
        return savePath.getParent();
    }

    @Override
    public Optional<GenerationTask> loadTask(final World world) {
        final Map<String, TaskModel> tasks = Optional.ofNullable(configModel.tasks).orElse(new HashMap<>());
        final TaskModel taskModel = tasks.get(world.getName());
        if (taskModel == null) {
            return Optional.empty();
        }
        final boolean cancelled = Optional.ofNullable(taskModel.cancelled).orElse(false);
        final double radiusX = Optional.ofNullable(taskModel.radius).orElse(Selection.DEFAULT_RADIUS);
        final double radiusZ = Optional.ofNullable(taskModel.radiusZ).orElse(radiusX);
        final Selection.Builder selection = Selection.builder(chunky.get(), world)
                .centerX(Optional.ofNullable(taskModel.centerX).orElse(Selection.DEFAULT_CENTER_X))
                .centerZ(Optional.ofNullable(taskModel.centerZ).orElse(Selection.DEFAULT_CENTER_Z))
                .radiusX(radiusX)
                .radiusZ(radiusZ)
                .pattern(Parameter.of(Optional.ofNullable(taskModel.iterator).orElse(PatternType.CONCENTRIC)))
                .shape(Optional.ofNullable(taskModel.shape).orElse(ShapeType.SQUARE));
        final long count = taskModel.count;
        final long time = taskModel.time;
        return Optional.of(new GenerationTask(chunky.get(), selection.build(), count, time, cancelled));
    }

    @Override
    public List<GenerationTask> loadTasks() {
        final List<GenerationTask> generationTasks = new ArrayList<>();
        chunky.get().getServer().getWorlds().forEach(world -> loadTask(world).ifPresent(generationTasks::add));
        return generationTasks;
    }

    @Override
    public void saveTask(final GenerationTask generationTask) {
        if (configModel.tasks == null) {
            configModel.tasks = new HashMap<>();
        }
        final Map<String, TaskModel> tasks = configModel.tasks;
        final Selection selection = generationTask.getSelection();
        final TaskModel taskModel = tasks.getOrDefault(selection.world().getName(), new TaskModel());
        final String shape = generationTask.getShape().name();
        taskModel.cancelled = generationTask.isCancelled();
        taskModel.radius = selection.radiusX();
        if (ShapeType.RECTANGLE.equals(shape) || ShapeType.ELLIPSE.equals(shape)) {
            taskModel.radiusZ = selection.radiusZ();
        }
        taskModel.centerX = selection.centerX();
        taskModel.centerZ = selection.centerZ();
        taskModel.iterator = generationTask.getChunkIterator().name();
        taskModel.shape = shape;
        taskModel.count = generationTask.getCount();
        taskModel.time = generationTask.getTotalTime();
        tasks.put(selection.world().getName(), taskModel);
        saveConfig();
    }

    @Override
    public void saveTasks() {
        chunky.get().getGenerationTasks().values().forEach(this::saveTask);
    }

    @Override
    public void cancelTask(final World world) {
        loadTask(world).ifPresent(generationTask -> {
            generationTask.stop(true);
            saveTask(generationTask);
        });
    }

    @Override
    public void cancelTasks() {
        loadTasks().forEach(generationTask -> {
            generationTask.stop(true);
            saveTask(generationTask);
        });
    }

    @Override
    public int getVersion() {
        return Optional.ofNullable(configModel.version).orElse(0);
    }

    @Override
    public String getLanguage() {
        return Optional.ofNullable(configModel.language).map(Input::checkLanguage).orElse("en");
    }

    @Override
    public boolean getContinueOnRestart() {
        return Optional.ofNullable(configModel.continueOnRestart).orElse(false);
    }

    @Override
    public boolean isSilent() {
        return Optional.ofNullable(configModel.silent).orElse(false);
    }

    @Override
    public void setSilent(final boolean silent) {
        configModel.silent = silent;
    }

    @Override
    public int getUpdateInterval() {
        return Optional.ofNullable(configModel.updateInterval).orElse(1);
    }

    @Override
    public void setUpdateInterval(final int updateInterval) {
        configModel.updateInterval = updateInterval;
    }

    @Override
    public void reload() {
        try (final Reader reader = Files.newBufferedReader(savePath)) {
            configModel = GSON.fromJson(reader, ConfigModel.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveConfig() {
        try {
            Files.createDirectories(savePath.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (final Writer writer = Files.newBufferedWriter(savePath)) {
            GSON.toJson(configModel, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private static class ConfigModel {
        private Integer version = 1;
        private String language = "en";
        private Boolean continueOnRestart = false;
        private Boolean silent = false;
        private Integer updateInterval = 1;
        private Map<String, TaskModel> tasks;

        public Integer getVersion() {
            return version;
        }

        public void setVersion(final Integer version) {
            this.version = version;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(final String language) {
            this.language = language;
        }

        public Boolean getContinueOnRestart() {
            return continueOnRestart;
        }

        public void setContinueOnRestart(final Boolean continueOnRestart) {
            this.continueOnRestart = continueOnRestart;
        }

        public Map<String, TaskModel> getTasks() {
            return tasks;
        }

        public void setTasks(final Map<String, TaskModel> tasks) {
            this.tasks = tasks;
        }

        public boolean isSilent() {
            return silent;
        }

        public void setSilent(final boolean silent) {
            this.silent = silent;
        }

        public int getUpdateInterval() {
            return updateInterval;
        }

        public void setUpdateInterval(final int updateInterval) {
            this.updateInterval = updateInterval;
        }
    }

    @SuppressWarnings("unused")
    private static class TaskModel {
        private Boolean cancelled;
        private Double radius;
        private Double radiusZ;
        private Double centerX;
        private Double centerZ;
        private String iterator;
        private String shape;
        private Long count;
        private Long time;

        public Boolean getCancelled() {
            return cancelled;
        }

        public void setCancelled(final Boolean cancelled) {
            this.cancelled = cancelled;
        }

        public Double getRadius() {
            return radius;
        }

        public void setRadius(final Double radius) {
            this.radius = radius;
        }

        public Double getRadiusZ() {
            return radiusZ;
        }

        public void setRadiusZ(final Double radiusZ) {
            this.radiusZ = radiusZ;
        }

        public Double getCenterX() {
            return centerX;
        }

        public void setCenterX(final Double centerX) {
            this.centerX = centerX;
        }

        public Double getCenterZ() {
            return centerZ;
        }

        public void setCenterZ(final Double centerZ) {
            this.centerZ = centerZ;
        }

        public String getIterator() {
            return iterator;
        }

        public void setIterator(final String iterator) {
            this.iterator = iterator;
        }

        public String getShape() {
            return shape;
        }

        public void setShape(final String shape) {
            this.shape = shape;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(final Long count) {
            this.count = count;
        }

        public Long getTime() {
            return time;
        }

        public void setTime(final Long time) {
            this.time = time;
        }
    }
}
