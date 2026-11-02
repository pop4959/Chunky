package org.popcraft.chunky.util;

import java.util.Optional;

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

    public static Optional<Object> getStaticField(final String clazz, final String name) {
        try {
            return Optional.ofNullable(Class.forName(clazz).getField(name).get(null));
        } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
            return Optional.empty();
        }
    }

    public static Optional<Object> invokeMethod(final Object instance, final String name) {
        try {
            return Optional.ofNullable(instance.getClass().getMethod(name).invoke(instance));
        } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
            return Optional.empty();
        }
    }
}
