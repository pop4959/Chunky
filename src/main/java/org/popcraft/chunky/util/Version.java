package org.popcraft.chunky.util;

import org.bukkit.Bukkit;

public class Version implements Comparable<Version> {

    public static final Version v1_13_2 = new Version(1, 13, 2);

    private static Version currentMinecraftVersion;
    private int major = 0, minor = 0, patch = 0;

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public Version(String version) {
        int dash = version.indexOf('-');
        if (dash > -1) {
            version = version.substring(0, dash);
        }
        String[] semVer = version.split("\\.");
        try {
            if (semVer.length > 0) {
                this.major = Integer.parseInt(semVer[0]);
            }
            if (semVer.length > 1) {
                this.minor = Integer.parseInt(semVer[1]);
            }
            if (semVer.length > 2) {
                this.patch = Integer.parseInt(semVer[2]);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    public static Version getCurrentMinecraftVersion() {
        if (currentMinecraftVersion == null) {
            currentMinecraftVersion = new Version(Bukkit.getVersion());
        }
        return currentMinecraftVersion;
    }

    public boolean isEqualTo(Version o) {
        return compareTo(o) == 0;
    }

    public boolean isHigherThan(Version o) {
        return compareTo(o) > 0;
    }

    public boolean isHigherThanOrEqualTo(Version o) {
        return compareTo(o) >= 0;
    }

    public boolean isLowerThan(Version o) {
        return compareTo(o) < 0;
    }

    public boolean isLowerThanOrEqualTo(Version o) {
        return compareTo(o) <= 0;
    }

    @Override
    public int compareTo(Version o) {
        if (this.major != o.major) {
            return this.major - o.major;
        }
        if (this.minor != o.minor) {
            return this.minor - o.minor;
        }
        return this.patch - o.patch;
    }
}
