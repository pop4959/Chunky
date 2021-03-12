package org.popcraft.chunky.platform;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.Selection;

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

public class FabricConfig implements Config {
    private final Chunky chunky;
    private final Gson gson;
    private final Path configPath;
    private ConfigModel configModel;

    public FabricConfig(Chunky chunky) {
        this.chunky = chunky;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "chunky.json");
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
        Selection selection = new Selection(chunky);
        selection.world = world;
        selection.radiusX = taskModel.radius;
        selection.radiusZ = taskModel.radiusZ == null ? taskModel.radius : taskModel.radiusZ;
        selection.centerX = taskModel.centerX;
        selection.centerZ = taskModel.centerZ;
        selection.pattern = taskModel.iterator;
        selection.shape = taskModel.shape;
        long count = taskModel.count;
        long time = taskModel.time;
        return Optional.of(new GenerationTask(chunky, selection, count, time));
    }

    @Override
    public List<GenerationTask> loadTasks() {
        List<GenerationTask> generationTasks = new ArrayList<>();
        chunky.getPlatform().getServer().getWorlds().forEach(world -> loadTask(world).ifPresent(generationTasks::add));
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
        TaskModel taskModel = tasks.getOrDefault(generationTask.getWorld().getName(), new TaskModel());
        String shape = generationTask.getShape().name();
        taskModel.cancelled = generationTask.isCancelled();
        taskModel.radius = generationTask.getRadiusX();
        if ("rectangle".equals(shape) || "oval".equals(shape)) {
            taskModel.radiusZ = generationTask.getRadiusZ();
        }
        taskModel.centerX = generationTask.getCenterX();
        taskModel.centerZ = generationTask.getCenterZ();
        taskModel.iterator = generationTask.getChunkIterator().name();
        taskModel.shape = shape;
        taskModel.count = generationTask.getCount();
        taskModel.time = generationTask.getTotalTime();
        tasks.put(generationTask.getWorld().getName(), taskModel);
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
        return this.configModel.version == null ? 0 : this.configModel.version;
    }

    @Override
    public String getLanguage() {
        return this.configModel.language == null ? "en" : this.configModel.language;
    }

    @Override
    public boolean getContinueOnRestart() {
        return this.configModel.continueOnRestart == null ? false : this.configModel.continueOnRestart;
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
        public Integer radius;
        public Integer radiusZ;
        public Integer centerX;
        public Integer centerZ;
        public String iterator;
        public String shape;
        public Long count;
        public Long time;
    }
}
