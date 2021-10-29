package org.popcraft.chunky.event;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class EventBus {
    private static final MethodHandle accept;

    static {
        MethodHandle acceptMethodHandle = null;
        try {
            acceptMethodHandle = MethodHandles.publicLookup().findVirtual(Consumer.class, "accept", MethodType.methodType(void.class, Object.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        accept = acceptMethodHandle;
    }

    private final Map<Class<?>, Set<Consumer<?>>> subscribers = new HashMap<>();

    public <T> void subscribe(final Class<T> eventClass, final Consumer<T> subscriber) {
        subscribers.computeIfAbsent(eventClass, x -> new HashSet<>());
        subscribers.get(eventClass).add(subscriber);
    }

    public <T> void unsubscribe(final Class<T> eventClass, final Consumer<T> subscriber) {
        subscribers.computeIfAbsent(eventClass, x -> new HashSet<>());
        subscribers.get(eventClass).remove(subscriber);
    }

    public void unsubscribeAll() {
        subscribers.clear();
    }

    public void unsubscribeAll(final Class<?> eventClass) {
        subscribers.remove(eventClass);
    }

    public void call(final Object event) {
        final Class<?> eventClass = event.getClass();
        if (accept == null || !subscribers.containsKey(eventClass)) {
            return;
        }
        subscribers.get(eventClass).forEach(subscriber -> {
            try {
                accept.invoke(subscriber, event);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }
}
