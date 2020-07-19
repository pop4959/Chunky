package org.popcraft.chunky;

public final class Constants {

    public final static String HELP_START = "§2chunky start§r - Start a new chunk generation task";
    public final static String HELP_PAUSE = "§2chunky pause§r - Pause current tasks and save progress";
    public final static String HELP_CONTINUE = "§2chunky continue§r - Continue current or saved tasks";
    public final static String HELP_CANCEL = "§2chunky cancel§r - Stop and delete current or saved tasks";
    public final static String HELP_WORLD = "§2chunky world <world>§r - Set the world target";
    public final static String HELP_CENTER = "§2chunky center <x> <z>§r - Set the center block location";
    public final static String HELP_RADIUS = "§2chunky radius <radius>§r - Set the radius";
    public final static String HELP_SILENT = "§2chunky silent§r - Toggle displaying update messages";
    public final static String HELP_QUIET = "§2chunky quiet <interval>§r - Set the quiet interval";
    public final static String HELP_MENU = String.format("§aChunky Commands§r\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s", HELP_START, HELP_PAUSE, HELP_CONTINUE, HELP_CANCEL, HELP_WORLD, HELP_CENTER, HELP_RADIUS, HELP_SILENT, HELP_QUIET);
    public final static String FORMAT_START = "[Chunky] Task started for %s at %d, %d with radius %d.";
    public final static String FORMAT_STARTED_ALREADY = "[Chunky] Task already started for %s!";
    public final static String FORMAT_PAUSE = "[Chunky] Task paused for %s.";
    public final static String FORMAT_CONTINUE = "[Chunky] Task continuing for %s.";
    public final static String FORMAT_WORLD = "[Chunky] World changed to %s.";
    public final static String FORMAT_CENTER = "[Chunky] Center changed to %d, %d.";
    public final static String FORMAT_RADIUS = "[Chunky] Radius changed to %d.";
    public final static String FORMAT_SILENT = "[Chunky] Silent mode %s.";
    public final static String FORMAT_QUIET = "[Chunky] Quiet interval set to %d seconds.";
    public final static String FORMAT_UPDATE = "[Chunky] Task running for %s. Processed: %d chunks (%.2f%%), ETA: %01d:%02d:%02d, Rate: %.1f cps, Current: %d, %d";
    public final static String FORMAT_DONE = "[Chunky] Task finished for %s. Processed: %d chunks (%.2f%%), Total time: %01d:%02d:%02d";
    public final static String FORMAT_STOPPED = "[Chunky] Task stopped for %s.";

    private Constants() {
        throw new IllegalStateException("Static Constant Class");
    }

}
