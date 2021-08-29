package org.popcraft.chunky.platform.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.Config;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.shape.ShapeType;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.Translator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class GsonConfig implements Config {
    private final Supplier<Chunky> chunky;
    private final Gson gson;
    private final Path configPath;
    private ConfigModel configModel;

    public GsonConfig(Supplier<Chunky> chunky, File configFile) {
        this.chunky = chunky;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.configPath = configFile.toPath();
        if (!configFile.exists()) {
            try {
                if (configFile.createNewFile()) {
                    this.configModel = new ConfigModel();
                    configModel.version = 1;
                    configModel.language = "en";
                    configModel.continueOnRestart = false;
                    saveConfig();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            reload();
        }
        Translator.setLanguage(getLanguage());
    }

    @Override
    public Path getDirectory() {
        return configPath.getParent();
    }

    @Override
    public Optional<GenerationTask> loadTask(World world) {
        if (this.configModel == null) {
            return Optional.empty();
        }
        Map<String, TaskModel> tasks = this.configModel.tasks;
        if (tasks == null) {
            return Optional.empty();
        }
        TaskModel taskModel = tasks.get(world.getName());
        if (taskModel == null || taskModel.cancelled) {
            return Optional.empty();
        }
        Selection.Builder selection = Selection.builder(world)
                .centerX(taskModel.centerX)
                .centerZ(taskModel.centerZ)
                .radiusX(taskModel.radius)
                .radiusZ(taskModel.radiusZ == null ? taskModel.radius : taskModel.radiusZ)
                .pattern(taskModel.iterator)
                .shape(taskModel.shape);
        long count = taskModel.count;
        long time = taskModel.time;
        return Optional.of(new GenerationTask(chunky.get(), selection.build(), count, time));
    }

    @Override
    public List<GenerationTask> loadTasks() {
        List<GenerationTask> generationTasks = new ArrayList<>();
        chunky.get().getServer().getWorlds().forEach(world -> loadTask(world).ifPresent(generationTasks::add));
        return generationTasks;
    }

    @Override
    public void saveTask(GenerationTask generationTask) {
        if (this.configModel == null) {
            this.configModel = new ConfigModel();
        }
        if (this.configModel.tasks == null) {
            this.configModel.tasks = new HashMap<>();
        }
        Map<String, TaskModel> tasks = this.configModel.tasks;
        Selection selection = generationTask.getSelection();
        TaskModel taskModel = tasks.getOrDefault(selection.world().getName(), new TaskModel());
        String shape = generationTask.getShape().name();
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
    public void cancelTask(World world) {
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
        return getConfigModel().map(model -> model.version).orElse(0);
    }

    @Override
    public String getLanguage() {
        return getConfigModel().map(model -> Input.checkLanguage(model.language)).orElse("en");
    }

    @Override
    public boolean getContinueOnRestart() {
        return getConfigModel().map(model -> model.continueOnRestart).orElse(false);
    }

    @Override
    public void reload() {
        StringBuilder configBuilder = new StringBuilder();
        try (Stream<String> input = Files.lines(configPath)) {
            input.forEach(configBuilder::append);
        } catch (IOException e) {
            e.printStackTrace();
        }
        configModel = gson.fromJson(configBuilder.toString(), new TypeToken<ConfigModel>() {
        }.getType());
    }

    public void saveConfig() {
        try (Writer writer = new BufferedWriter(new FileWriter(configPath.toFile()))) {
            gson.toJson(configModel, new TypeToken<ConfigModel>() {
            }.getType(), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Optional<ConfigModel> getConfigModel() {
        return Optional.ofNullable(configModel);
    }

    @SuppressWarnings("unused")
    public static class ConfigModel {
        private Integer version;
        private String language;
        private Boolean continueOnRestart;
        private Map<String, TaskModel> tasks;

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public Boolean getContinueOnRestart() {
            return continueOnRestart;
        }

        public void setContinueOnRestart(Boolean continueOnRestart) {
            this.continueOnRestart = continueOnRestart;
        }

        public Map<String, TaskModel> getTasks() {
            return tasks;
        }

        public void setTasks(Map<String, TaskModel> tasks) {
            this.tasks = tasks;
        }
    }


    @SuppressWarnings("unused")
    public static class TaskModel {
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

        public void setCancelled(Boolean cancelled) {
            this.cancelled = cancelled;
        }

        public Double getRadius() {
            return radius;
        }

        public void setRadius(Double radius) {
            this.radius = radius;
        }

        public Double getRadiusZ() {
            return radiusZ;
        }

        public void setRadiusZ(Double radiusZ) {
            this.radiusZ = radiusZ;
        }

        public Double getCenterX() {
            return centerX;
        }

        public void setCenterX(Double centerX) {
            this.centerX = centerX;
        }

        public Double getCenterZ() {
            return centerZ;
        }

        public void setCenterZ(Double centerZ) {
            this.centerZ = centerZ;
        }

        public String getIterator() {
            return iterator;
        }

        public void setIterator(String iterator) {
            this.iterator = iterator;
        }

        public String getShape() {
            return shape;
        }

        public void setShape(String shape) {
            this.shape = shape;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        public Long getTime() {
            return time;
        }

        public void setTime(Long time) {
            this.time = time;
        }
    }
}
