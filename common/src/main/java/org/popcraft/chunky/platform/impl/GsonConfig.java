package org.popcraft.chunky.platform.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.Config;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.util.Input;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GsonConfig implements Config {
    private final Chunky chunky;
    private final Gson gson;
    private final Path configPath;
    private ConfigModel configModel;

    public GsonConfig(Chunky chunky, File configFile) {
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
        return Optional.of(new GenerationTask(chunky, selection.build(), count, time));
    }

    @Override
    public List<GenerationTask> loadTasks() {
        List<GenerationTask> generationTasks = new ArrayList<>();
        chunky.getServer().getWorlds().forEach(world -> loadTask(world).ifPresent(generationTasks::add));
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
        if ("rectangle".equals(shape) || "ellipse".equals(shape)) {
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
        chunky.getGenerationTasks().values().forEach(this::saveTask);
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
        return getConfigModel().map(configModel -> configModel.version).orElse(0);
    }

    @Override
    public String getLanguage() {
        return getConfigModel().map(configModel -> Input.checkLanguage(configModel.language)).orElse("en");
    }

    @Override
    public boolean getContinueOnRestart() {
        return getConfigModel().map(configModel -> configModel.continueOnRestart).orElse(false);
    }

    @Override
    public void reload() {
        StringBuilder configBuilder = new StringBuilder();
        try {
            Files.lines(configPath).forEach(configBuilder::append);
        } catch (IOException e) {
            e.printStackTrace();
        }
        configModel = gson.fromJson(configBuilder.toString(), new TypeToken<ConfigModel>() {
        }.getType());
    }

    public void saveConfig() {
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            gson.toJson(configModel, new TypeToken<ConfigModel>() {
            }.getType(), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Optional<ConfigModel> getConfigModel() {
        return Optional.ofNullable(configModel);
    }

    public static class ConfigModel {
        public Integer version;
        public String language;
        public Boolean continueOnRestart;
        public Map<String, TaskModel> tasks;
    }

    public static class TaskModel {
        public Boolean cancelled;
        public Double radius;
        public Double radiusZ;
        public Double centerX;
        public Double centerZ;
        public String iterator;
        public String shape;
        public Long count;
        public Long time;
    }
}
