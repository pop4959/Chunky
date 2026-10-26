package org.popcraft.chunky.util;

public final class Reflection {
    private Reflection() {
    }

    public static boolean classExists(final String clazz) {
        try {
            Class.forName(clazz);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean methodExists(final Class<?> clazz, final String name, final Class<?>... parameterTypes) {
        try {
            clazz.getMethod(name, parameterTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
