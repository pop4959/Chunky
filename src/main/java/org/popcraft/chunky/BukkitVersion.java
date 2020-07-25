package org.popcraft.chunky;

import org.bukkit.Bukkit;

public class BukkitVersion implements Comparable<BukkitVersion> {

    public static final BukkitVersion v1_13_2 = new BukkitVersion(1, 13, 2);

    private static BukkitVersion currentVersion;
    private int major = 0, minor = 0, patch = 0;

    public BukkitVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public BukkitVersion(String version) {
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

    public static BukkitVersion getCurrent() {
        if (currentVersion == null) {
            currentVersion = new BukkitVersion(Bukkit.getBukkitVersion());
        }
        return currentVersion;
    }

    public boolean isEqualTo(BukkitVersion o) {
        return compareTo(o) == 0;
    }

    public boolean isHigherThan(BukkitVersion o) {
        return compareTo(o) > 0;
    }

    public boolean isHigherThanOrEqualTo(BukkitVersion o) {
        return compareTo(o) >= 0;
    }

    public boolean isLowerThan(BukkitVersion o) {
        return compareTo(o) < 0;
    }

    public boolean isLowerThanOrEqualTo(BukkitVersion o) {
        return compareTo(o) <= 0;
    }

    @Override
    public int compareTo(BukkitVersion o) {
        if (this.major != o.major) {
            return this.major - o.major;
        }
        if (this.minor != o.minor) {
            return this.minor - o.minor;
        }
        return this.patch - o.patch;
    }
}
