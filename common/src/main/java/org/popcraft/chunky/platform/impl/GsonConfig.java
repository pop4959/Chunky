package org.popcraft.chunky.platform.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.popcraft.chunky.platform.Config;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.Translator;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class GsonConfig implements Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path savePath;
    private ConfigModel configModel = new ConfigModel();

    public GsonConfig(final Path savePath) {
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
        private Integer version = 2;
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
