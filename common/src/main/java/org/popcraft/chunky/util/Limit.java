package org.popcraft.chunky.util;

import org.popcraft.chunky.platform.Config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Limit {
    private static double limit = Integer.MAX_VALUE;

    public static void set(final Config config) {
        final Path limitFile = config.getDirectory().resolve(".chunky.properties");
        try (final InputStream input = Files.newInputStream(limitFile)) {
            final Properties properties = new Properties();
            properties.load(input);
            Input.tryDouble(properties.getProperty("radius-limit")).ifPresent(radius -> limit = radius);
        } catch (IOException ignored) {
        }
    }

    public static double get() {
        return limit;
    }
}
