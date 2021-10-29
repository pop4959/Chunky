package org.popcraft.chunky.util;

import java.util.Objects;

public class Version implements Comparable<Version> {
    public static final Version INVALID = new Version(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    public static final Version MINECRAFT_1_13_2 = new Version(1, 13, 2);
    private int major = 0, minor = 0, patch = 0;

    public Version(final int major, final int minor, final int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public Version(final String version) {
        if (version == null || version.isEmpty()) {
            this.major = Integer.MIN_VALUE;
            return;
        }
        final String[] semVer = version.split("\\.");
        if (semVer.length > 0) {
            this.major = Input.tryInteger(semVer[0]).orElse(Integer.MIN_VALUE);
        }
        if (semVer.length > 1) {
            this.minor = Input.tryInteger(semVer[1]).orElse(Integer.MIN_VALUE);
        }
        if (semVer.length > 2) {
            this.patch = Input.tryInteger(semVer[2]).orElse(Integer.MIN_VALUE);
        }
    }

    public Version(final String version, boolean minecraft) {
        this(minecraft && version.indexOf('-') > -1 ? version.substring(0, version.indexOf('-')) : version);
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

    public boolean isValid() {
        return major != Integer.MIN_VALUE && minor != Integer.MIN_VALUE && patch != Integer.MIN_VALUE;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version version = (Version) o;
        return major == version.major && minor == version.minor && patch == version.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }
}
