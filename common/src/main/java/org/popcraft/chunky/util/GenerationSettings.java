package org.popcraft.chunky.util;

public final class GenerationSettings {
    private GenerationSettings() {
    }

    public static int maxWorkingCount() {
        return Input.tryInteger(System.getProperty("chunky.maxWorkingCount")).orElse(50);
    }
}
